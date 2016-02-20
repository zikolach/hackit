package hackit.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hackit.Status
import upickle.default._

import scala.concurrent.duration._
import scala.language.postfixOps

class HttpService(implicit system: ActorSystem, materializer: ActorMaterializer) {

  implicit val timeout: Timeout = 10 seconds

  val route: Route =
    pathSingleSlash {
      getFromResource("web/index.html")
    } ~
      path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
      path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
      getFromResourceDirectory("web") ~
      pathPrefix("api") {
        get {
          pathSuffix("status")
          complete {
            write(Status("ok"))
          }
        }
      }

}
