package hackit

import org.scalajs.dom.document._
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{HTMLDivElement, HTMLImageElement}

import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.util.Random

class GameMap {
  var offset: (Double, Double) = (0, 0)
  val tileSize = (32, 26)
  val tileOffset = 8

  val tiles = getElementsByClassName("tiles").asInstanceOf[HTMLDivElement]

  private val terrainNames = "grass" :: "tree" :: "mountain" :: "sea" :: Nil
  private val settlementNames = "village" :: "fort" :: Nil

  val textures: Map[String, HTMLImageElement] = (terrainNames ++ settlementNames).map { name =>
    val img = createElement("img").asInstanceOf[HTMLImageElement]
    img.src = s"img/tile_$name.png"
    name -> img
  } toMap

  val terrains: Map[(Int, Int), String] = generateTerrain

  private def generateTerrain: Map[(Int, Int), String] = {
    (for {
      x <- 1 to 16
      y <- 1 to 14
      kind = terrainNames(Random.nextInt(terrainNames.size))
    } yield (x, y) -> kind).toMap
  }

  def render(canvas: Canvas)(millis: Double): Unit = {
    val ctx = canvas.getContext("2d")
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    drawTerrain(ctx)
  }

  def update(dt: Double) = {

  }

  private def calcOffset(pos: (Int, Int)) = {
    val halfHeight = tileSize._2 / 2
    if (pos._1 % 2 == 0) {
      ((tileSize._1 - tileOffset) * pos._1, tileSize._2 * pos._2 + halfHeight)
    } else {
      ((tileSize._1 - tileOffset) * pos._1, tileSize._2 * pos._2)
    }
  }

  private def drawTerrain(ctx: Dynamic): Unit = {
    terrains.foreach { tile =>
      val offset = calcOffset(tile._1)
      ctx.drawImage(textures(tile._2), offset._1, offset._2)
    }
  }
}
