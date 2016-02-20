package hackit

import org.scalajs.dom.document._
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLDivElement, HTMLImageElement, MouseEvent}

import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.util.Random

case class GameMap(canvas: HTMLCanvasElement, width: Int, height: Int) {
  val tileSize = (32, 26)
  val tileOffset = 8

  var offset: (Double, Double) = {
    val gridWidth = tileSize._1 - tileOffset
    val gridHeight = tileSize._2 / 2
    (canvas.width / 2 - (width / 2 * gridWidth), canvas.height / 2 - (height * gridHeight))
  }

  val tiles = getElementsByClassName("tiles").asInstanceOf[HTMLDivElement]

  private val terrainNames = "grass" :: "tree" :: "mountain" :: "sea" :: Nil
  private val settlementNames = "village" :: "fort" :: Nil

  val textures: Map[String, HTMLImageElement] = (terrainNames ++ settlementNames).map { name =>
    val img = createElement("img").asInstanceOf[HTMLImageElement]
    img.src = s"img/tile_$name.png"
    name -> img
  } toMap

  val terrains: Map[(Int, Int), String] = generateTerrain

  var mouseDown: Option[(Double, Int, Int)] = None

  canvas.onmousedown = (e: MouseEvent) => {
    println(s" ${e.clientX.toInt}, ${e.clientY.toInt}")
    mouseDown = Some((e.timeStamp, e.clientX.toInt, e.clientY.toInt))
  }

  canvas.onmousemove = (e: MouseEvent) => {
    mouseDown match {
      case Some((timeStamp, x, y)) =>
        println("drag")
        offset = (offset._1 + (e.clientX - x), offset._2 + (e.clientY - y))
        mouseDown = Some((e.timeStamp, e.clientX.toInt, e.clientY.toInt))
      case _ =>
    }
  }

  canvas.onmouseup = (e: MouseEvent) => {
    mouseDown = None
  }

  private def generateTerrain: Map[(Int, Int), String] = {
    (for {
      x <- 0 to width
      y <- 0 to height
      kind = terrainNames(Random.nextInt(terrainNames.size))
    } yield (x, y) -> kind).toMap
  }

  def render(millis: Double): Unit = {
    val ctx = canvas.getContext("2d")
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    drawTerrain(ctx)
  }

  def update(dt: Double) = {

  }

  private def calcOffset(pos: (Int, Int)) = {
    val x = (offset._1 + (tileSize._1 - tileOffset) * pos._1) - tileSize._1 / 2
    val y = (offset._2 + tileSize._2 * pos._2) - tileSize._2 / 2
    if (pos._1 % 2 == 0) {
      val halfHeight = tileSize._2 / 2
      (x, y + halfHeight)
    } else {
      (x, y)
    }
  }

  private def drawTerrain(ctx: Dynamic): Unit = {
    terrains.foreach { tile =>
      val pos = calcOffset(tile._1)
      ctx.drawImage(textures(tile._2), pos._1, pos._2)
    }
  }
}
