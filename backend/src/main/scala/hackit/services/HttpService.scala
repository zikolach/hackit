package hackit.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.stream.{OverflowStrategy, ActorMaterializer}
import akka.stream.scaladsl.{Source, Sink, Flow}
import akka.util.Timeout
import hackit.actors.GameMaster
import hackit.actors.GameMaster._
import hackit.commands.{JoinGame, CreateGame}
import hackit._
import upickle.default._

import scala.concurrent.duration._
import scala.language.postfixOps

class HttpService(implicit system: ActorSystem, materializer: ActorMaterializer) {

  import system.dispatcher

  implicit val timeout: Timeout = 10 seconds

  val gameMaster = system.actorOf(GameMaster.props)

  val route: Route =
    pathSingleSlash {
      getFromResource("web/index.html")
    } ~
      path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
      path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
      getFromResourceDirectory("web") ~
      pathPrefix("api") {
        extractClientIP { clientIP =>
          get {
            pathSuffix("status") {
              complete {
                write(Status("ok"))
              }
            }
          } ~
            pathPrefix("games") {
              pathEndOrSingleSlash {
                get {
                  complete {
                    val listFuture = (gameMaster ? ListGames).mapTo[GameList]
                    listFuture.map { list =>
                      write(list)
                    }
                  }
                } ~
                  (post & entity(as[String])) { body =>
                    complete {
                      val gameCreateCommand = read[CreateGame](body)
                      (gameMaster ? gameCreateCommand).mapTo[GameDesc].map { game => write(game) }
                    }

                  }
              } ~
                pathPrefix(Segment) { gameId =>
                  (path("join") & post & entity(as[String])) { body =>
                    complete {
                      val gameJoinCommand = read[JoinGame](body)
                      (gameMaster ? gameJoinCommand).mapTo[GameDesc].map { game => write(game) }
                    }
                  }
                }
            } ~
            path("updates" / Segment) { gameId =>
              val in = Flow[GameMessage].to(Sink.actorRef[GameMessage](gameMaster, GameDisconnected(clientIP.toString())))
              val out = Source.actorRef[Packet](1, OverflowStrategy.fail)
                .mapMaterializedValue(gameMaster ! GameConnected(gameId, clientIP.toString(), _))
              handleWebsocketMessages(Flow[Message]
                .collect { case TextMessage.Strict(msg) => GameUpdate(read[Packet](msg)) }
                .via(Flow.fromSinkAndSource(in, out))
                .map { case msg: GameMapUpdate => TextMessage.Strict(write(msg)) }
              )
            }
        }
      }

}
