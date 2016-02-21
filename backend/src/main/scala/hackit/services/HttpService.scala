package hackit.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hackit.actors.GameMaster
import hackit.actors.GameMaster.ListGames
import hackit.commands.CreateGame
import hackit.{GameList, GameShort, Status}
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
        get {
          pathSuffix("status") {
            complete {
              write(Status("ok"))
            }
          }
        } ~
          path("games") {
            get {
              complete {
                val listFuture = (gameMaster ? ListGames).mapTo[GameList]
                listFuture.map { list =>
                  write(list)
                }
              }
            } ~
            post {
              entity(as[String]) { body =>
                complete {
                  val gameCreateCommand = read[CreateGame](body)
                  val gameFuture = (gameMaster ? CreateGame).mapTo[GameShort]
                  gameFuture.map { game =>
                    write(game)
                  }
                }
              }
            }
          }
      }

}
