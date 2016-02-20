package hackit

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.concurrent.JSExecutionContext.Implicits
import scala.scalajs.js
import org.scalajs.dom.document._
import org.scalajs.dom.window._

object Frontend extends js.JSApp {

  implicit val ec = Implicits.queue

  val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)

  val canvas = {
    val c = document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    c.width = window.innerWidth
    c.height = window.innerHeight
    document.body.appendChild(c)
    c
  }
  var lastTime: Double = 0
  val game = new GameMap(canvas, 100, 100)

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