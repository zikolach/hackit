package hackit

import org.scalajs.dom.document._
import org.scalajs.dom.window
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLDivElement, HTMLImageElement, MouseEvent}

import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.util.Random

case class PlayerStats(color: String, villages: Seq[(Int, Int)], forts: Seq[(Int, Int)])

case class GameStats(turn: Int = 0, players: Seq[PlayerStats])

case class GameMap(canvas: HTMLCanvasElement, width: Int, height: Int) {
  val tileSize = (32, 26)
  val tileOffset = 8

  var gameStats = GameStats(0, Seq(PlayerStats("red", Seq.empty, Seq.empty)))

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

  val highlightHex = {
    val img = createElement("img").asInstanceOf[HTMLImageElement]
    img.src = s"img/trans.png"
    img
  }

  val Seq(board, food, wood, stone) = ("board" :: "food" :: "wood" :: "stone" :: Nil).map { name =>
    val img = createElement("img").asInstanceOf[HTMLImageElement]
    img.src = s"img/$name.png"
    img
  }

  val Seq(village, fort) = ("village" :: "fort" :: Nil).map { name =>
    val img = createElement("img").asInstanceOf[HTMLImageElement]
    img.src = s"img/tile_$name.png"
    img
  }

  val terrains: Map[(Int, Int), String] = generateTerrain
  var highlightedTile: Option[(Int, Int)] = None

  var mouseDown: Option[(Double, Int, Int)] = None

  canvas.onmousedown = (e: MouseEvent) => {
    mouseDown = Some((e.timeStamp, e.clientX.toInt, e.clientY.toInt))
  }

  canvas.onmousemove = (e: MouseEvent) => {
    mouseDown match {
      case Some((timeStamp, x, y)) =>
//        println("drag")
        offset = (offset._1 + (e.clientX - x), offset._2 + (e.clientY - y))
        mouseDown = Some((timeStamp, e.clientX.toInt, e.clientY.toInt))
      case _ =>
    }
    val tilePos = calcHex((e.clientX.toInt, e.clientY.toInt))
    highlightedTile = Some(tilePos)
//    println(highlightedTile)
  }

  canvas.onmouseup = (e: MouseEvent) => {
    mouseDown match {
      case Some((timeStamp, x, y)) if e.timeStamp - timeStamp < 500 && e.clientX - x < 5 && e.clientY - y < 5 =>
//        println(s"click ${e.clientX}:${e.clientY}")
        val pos = calcHex((e.clientX.toInt, e.clientY.toInt))
        gameStats = gameStats.copy(players = gameStats.players.map(player => {
          player.copy(villages = player.villages :+ pos)
        }))
      case _ =>
    }
    mouseDown = None
  }

  canvas.onmouseleave = canvas.onmouseup

  private def generateTerrain: Map[(Int, Int), String] = {
    (for {
      x <- 0 to width
      y <- 0 to height
      kind = terrainNames(Random.nextInt(terrainNames.size))
    } yield (x, y) -> kind).toMap
  }

  def drawScoreboard(ctx: Dynamic): Unit = {
    val centerX = canvas.width / 2
    val panelW = 296
    val panelH = 137
    ctx.drawImage(board, centerX - panelW / 2, 0)
    Seq(food, wood, stone).zipWithIndex.foreach { case (img, index) =>
      val x = centerX - (64 * 3) / 2 + index * 64
      val y = 32
      ctx.drawImage(img, x, y)
      ctx.beginPath()
      ctx.arc(x + 48, y + 48, 12, 0, 2 * Math.PI)
      ctx.fill()
      val fillStyle = ctx.fillStyle
      ctx.fillStyle = "#FFFFFF"
      ctx.textAlign = "center"
      ctx.textBaseline = "middle"
      ctx.font = "15px Arial"
      ctx.fillText("99", x + 48, y + 48)
      ctx.fillStyle = fillStyle
    }

  }

  def render(millis: Double): Unit = {
    val ctx = canvas.getContext("2d")
    ctx.canvas.width  = window.innerWidth
    ctx.canvas.height = window.innerHeight
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    drawTerrain(ctx)
    drawScoreboard(ctx)
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

  private def calcHex(pos: (Int, Int)): (Int, Int) = {
    val x = ((pos._1 - offset._1) / (tileSize._1 - tileOffset)).toInt
    val y = ((pos._2 - offset._2 - (if (x % 2 == 0) tileSize._2 / 2 else 0)) / tileSize._2).toInt
    (x, y)
  }

  private def drawTerrain(ctx: Dynamic): Unit = {
    terrains.foreach { tile =>
      val pos = calcOffset(tile._1)
      val Pos = tile._1
      ctx.drawImage(textures(tile._2), pos._1, pos._2)
      highlightedTile match {
        case Some(Pos) =>
          ctx.drawImage(highlightHex, pos._1, pos._2)
        case _ =>

      }

    }
    gameStats.players.foreach { player =>
      player.villages.foreach { villagePos =>
        val pos = calcOffset(villagePos)
        ctx.drawImage(village, pos._1, pos._2)
      }
    }
  }
}
