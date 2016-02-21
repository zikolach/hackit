package hackit

import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.MessageEvent
import upickle.default._

trait Updater {
  def ws: WebSocket

  def send(packet: Packet): Unit = ws.send(write(packet))

  def recv(handler: Packet => Unit) = ws.onmessage = (msg: MessageEvent) => handler(read[Packet](msg.data.toString))
}
