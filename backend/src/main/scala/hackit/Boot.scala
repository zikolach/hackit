package hackit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import hackit.services.HttpService

import scala.util.{Failure, Success}

object Boot extends App with Directives {
  implicit val system = ActorSystem()

  import system.dispatcher

  implicit val materializer = ActorMaterializer()

  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val httpService = new HttpService()

  val binding = Http().bindAndHandle(httpService.route, interface, port)
  binding.onComplete {
    case Success(b) =>
      val localAddress = b.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) =>
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
