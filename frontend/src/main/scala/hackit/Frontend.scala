package hackit

import hackit.commands.{JoinGame, CreateGame}
import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData._
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{MessageEvent, WebSocket}
import org.scalajs.dom.window._
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits
import scala.scalajs.js
import scala.util.Random




object Frontend extends js.JSApp {

  val playerName = Random.alphanumeric.filter(_.isLetter).map(_.toLower).take(5).mkString

  implicit val ec = Implicits.queue

  val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)


  def startGame(gameDesc: GameDesc): Unit = {
    val canvas = {
      val c = document.createElement("canvas").asInstanceOf[Canvas]
      c.width = window.innerWidth
      c.height = window.innerHeight
      document.body.appendChild(c)
      c
    }
    var lastTime: Double = 0
    val game = new GameMap(gameDesc, playerName, canvas, 10, 10)

    def gameLoop(millis: Double): Unit = {
      val dt = if (lastTime == 0) 0.0 else millis - lastTime
      lastTime = millis
      game.update(dt)
      game.render(dt)
      requestAnimationFrame(gameLoop _)
    }

    gameLoop(0)
  }

  def main(): Unit = {

    val menu = new GameMenu()
    Ajax.get("/api/games").onSuccess { case games =>
      val list = read[GameList](games.responseText)
      menu.refreshGames(list)
    }
    menu.onCreate(() => {
      Ajax.post("/api/games", str2ajax(write(CreateGame(generateGameId, playerName)))).onSuccess { case xhr =>
        val gameSHort = read[GameDesc](xhr.responseText)
        menu.cleanup()
        startGame(gameSHort)
      }
    })

    menu.onJoin(id => {
      Ajax.post(s"/api/games/$id/join", str2ajax(write(JoinGame(id, playerName)))).onSuccess { case xhr =>
        val gameSHort = read[GameDesc](xhr.responseText)
        menu.cleanup()
        startGame(gameSHort)
      }
    })
  }

  def generateGameId: String = {
    Random.alphanumeric.take(10).mkString
  }
}