package hackit

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{Element, HTMLButtonElement, HTMLDivElement}
import org.scalajs.dom.{MouseEvent, document}
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits

class GameMenu {

  import Implicits.queue

  val mainMenu = document.createElement("div").asInstanceOf[HTMLDivElement]
  mainMenu.id = "main-menu"

  var onCreateHandlerOpt: Option[() => Unit] = None
  var onJoinHandlerOpt: Option[String => Unit] = None

  val btn = document.createElement("div").asInstanceOf[HTMLDivElement]
  btn.innerHTML = "Start new game"
  btn.id = "start"
  btn.onclick = (e: MouseEvent) => {
    onCreateHandlerOpt.foreach(_ ())
  }
  mainMenu.appendChild(btn)

  document.body.appendChild(mainMenu)

  def onCreate(handler: () => Unit): Unit = {
    onCreateHandlerOpt = Some(handler)
  }

  def onJoin(handler: String => Unit): Unit = {
    onJoinHandlerOpt = Some(handler)
  }

  def cleanup(): Unit = {
    val mm = document.getElementById("main-menu")
    mm.parentNode.removeChild(mm)
  }

  def refreshGames(list: GameList): Unit = {
    val gameList = Option(document.getElementById("game-list").asInstanceOf[HTMLDivElement])
      .getOrElse(document.createElement("div").asInstanceOf[HTMLDivElement])

    gameList.id = "game-list"

    list.games.foreach { game =>
      Option(document.getElementById(game.id).asInstanceOf[HTMLDivElement])
        .getOrElse({
          val newItem = document.createElement("div").asInstanceOf[HTMLDivElement]
          newItem.id = game.id
          newItem.innerHTML = s"Join ${game.id}..."
          gameList.insertBefore(newItem, gameList.firstChild)
          newItem.onclick = (e: MouseEvent) => {
            onJoinHandlerOpt.foreach(_ (game.id))
          }
          newItem
        })
    }
    mainMenu.appendChild(gameList)
  }
}
