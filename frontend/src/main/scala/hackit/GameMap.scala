package hackit

import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLDivElement, HTMLImageElement, MouseEvent}
import org.scalajs.dom.{WebSocket, window}

import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.util.Random

case class GameMap(game: GameDesc, playerName: String, canvas: HTMLCanvasElement, width: Int, height: Int) {
  println(s"Game ${game.id} starting...")
  val tileSize = (128, 111)
  val tileOffset = 32

  val updater = new Updater {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    val ws: WebSocket = new WebSocket(s"$wsProtocol://${dom.document.location.host}/api/updates/${game.id}")
  }

  var gameStats = GameStats(game.id, 0, Seq.empty)


  private val (tileHalfWidth, tileHalfHeight) = (tileSize._1 / 2, tileSize._2 / 2)

  var offset: (Double, Double) = {
    val gridWidth = tileSize._1 - tileOffset
    val gridHeight = tileHalfHeight
    (canvas.width / 2 - (width / 2 * gridWidth), canvas.height / 2 - (height * gridHeight))
  }

  val tiles = getElementsByClassName("tiles").asInstanceOf[HTMLDivElement]

  import Settings._

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

  var terrains: Map[(Int, Int), String] = Map.empty

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

        updater.send(BuildVillage(game.id, playerName, pos._1, pos._2))
      //        gameStats = gameStats.copy(players = gameStats.players.map(player => {
      //          player.copy(villages = player.villages :+ pos)
      //        }))
      case _ =>
    }
    mouseDown = None
  }

  canvas.onmouseleave = canvas.onmouseup


  def drawScoreboard(ctx: Dynamic): Unit = {
    gameStats.players.find(_.playerName == playerName).foreach { playerStats =>
      val centerX = canvas.width / 2
      val panelW = 296
      val panelH = 137
      ctx.drawImage(board, centerX - panelW / 2, 0)
      Seq(food, wood, stone)
        .zip(Seq(playerStats.food, playerStats.wood, playerStats.stone))
        .zipWithIndex.foreach { case ((img, value), index) =>
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
        ctx.fillText(value.toString, x + 48, y + 48)
        ctx.fillStyle = fillStyle
      }
    }


  }

  def render(millis: Double): Unit = {
    val ctx = canvas.getContext("2d")
    ctx.canvas.width = window.innerWidth
    ctx.canvas.height = window.innerHeight
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    drawTerrain(ctx)
    drawScoreboard(ctx)
  }

  def update(dt: Double) = {

  }

  private def calcOffset(pos: (Int, Int)) = {
    val x = (offset._1 + (tileSize._1 - tileOffset) * pos._1) - tileHalfWidth
    val y = (offset._2 + tileSize._2 * pos._2) - tileHalfHeight
    if (pos._1 % 2 == 0) {
      val halfHeight = tileHalfHeight
      (x, y + halfHeight)
    } else {
      (x, y)
    }
  }

  private def calcHex(pos: (Int, Int)): (Int, Int) = {
    val x = ((pos._1 - offset._1 + tileHalfWidth) / (tileSize._1 - tileOffset)).toInt
    val y = ((pos._2 - offset._2 + (if (x % 2 == 0) 0 else tileHalfHeight)) / tileSize._2).toInt
    (x, y)
  }

  private def drawTerrain(ctx: Dynamic): Unit = {
    println("render")
    terrains.foreach { tile =>
      val pos = calcOffset(tile._1)
      ctx.drawImage(textures(tile._2), pos._1, pos._2)
    }
    gameStats.players.foreach { player =>
      player.villages.foreach { villagePos =>
        val pos = calcOffset(villagePos)
        ctx.drawImage(village, pos._1, pos._2)
      }
    }
    highlightedTile match {
      case Some(tile) =>
        val pos = calcOffset(tile)
        ctx.drawImage(highlightHex, pos._1, pos._2)
      case _ =>

    }
  }

  updater.recv {
    case GameMapUpdate(cells) =>
      terrains = cells.map(mc => (mc.x, mc.y) -> mc.terrain).toMap
      println("map updated")
    case PlayerStatsUpdate(playerStats) =>
      val players = gameStats.players.span(_.playerName == playerStats.playerName) match {
        case (_, others) => others :+ playerStats
      }
      gameStats = gameStats.copy(players = players)
      println(s"player ${gameStats.players} stats update")
    case packet => println(packet)
  }
}
