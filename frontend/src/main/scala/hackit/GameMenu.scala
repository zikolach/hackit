package hackit

import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.dom.raw.HTMLButtonElement

class GameMenu {

  var onStartHandlerOpt: Option[() => Unit] = None

  val btn = document.createElement("button").asInstanceOf[HTMLButtonElement]
  btn.innerHTML = "Start"
  btn.onclick = (e: MouseEvent) => {
    document.body.removeChild(btn)
    onStartHandlerOpt.foreach(_.apply())
  }
  document.body.appendChild(btn)

  def onStart(handler: () => Unit): Unit = {
    onStartHandlerOpt = Some(handler)
  }

}
