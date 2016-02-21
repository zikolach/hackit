package hackit.actors

import akka.actor.{Props, Actor}
import hackit.{GameShort, GameList, GameStats}
import hackit.actors.GameMaster.ListGames
import hackit.commands.CreateGame

import scala.util.Random

class GameMaster extends Actor {

  var games: Seq[GameStats] = Seq.empty

  def receive: Receive = {
    case ListGames =>
      sender ! GameList(games.map(game => GameShort(game.id, game.toString)))
    case CreateGame(name) =>
      games = games :+ GameStats(Random.alphanumeric.take(10).mkString, 0, Seq.empty)
  }
}

object GameMaster {
  def props = Props(classOf[GameMaster])

  sealed trait GameMasterCommand

  case object ListGames extends GameMasterCommand


  sealed trait GameMasterResponse



}