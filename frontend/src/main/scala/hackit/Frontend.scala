package hackit

import org.scalajs.dom.html.Canvas

import scala.scalajs.concurrent.JSExecutionContext.Implicits
import scala.scalajs.js
import org.scalajs.dom.document._
import org.scalajs.dom.window._

object Frontend extends js.JSApp {

  implicit val ec = Implicits.queue

  case class PlayerStats(color: String, villages: Seq[(Int, Int)], forts: Seq[(Int, Int)])

  case class GameStats(turn: Int = 0, players: Seq[PlayerStats])

  val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)
  val canvas = getElementById("main").asInstanceOf[Canvas]
  var lastTime: Double = 0
  val game = new GameMap(canvas, 50, 50)

  val gameLoop: Double => Unit = millis => {
    val dt = if (lastTime == 0) 0.0 else millis - lastTime
    lastTime = millis
    game.update(dt)
    game.render(dt)
    requestAnimationFrame(gameLoop)
  }

  def main(): Unit = {
    gameLoop(0)
  }
}