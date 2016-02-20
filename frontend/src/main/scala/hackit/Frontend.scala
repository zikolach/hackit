package hackit

import scala.scalajs.concurrent.JSExecutionContext.Implicits
import scala.scalajs.js

object Frontend extends js.JSApp {

  implicit val ec = Implicits.runNow

  def main(): Unit = {
    println("hello")
  }
}