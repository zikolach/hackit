package hackit.actors

import akka.actor.{ActorRef, Actor, Props}
import hackit.actors.GameMaster.{GameConnected, ListGames}
import hackit.commands.{GameNotFound, JoinGame, CreateGame}
import hackit._

class GameMaster extends Actor {

  var games: Map[String, (GameStats, Seq[MapCell])] = Map.empty

  import Settings._

  def receive: Receive = {
    case ListGames =>
      sender ! GameList(games.values.map({ case (game, _) => GameDesc(game.id, game.toString) }).toSeq)
    case CreateGame(id, playerName) =>
      val gameStats = GameStats(id, 0, Seq(PlayerStats(playerName, Seq.empty, Seq.empty)))
      games = games + (id ->(gameStats, generateTerrain(10, 10)))
      sender ! GameDesc(gameStats.id, gameStats.toString)
    case JoinGame(id, playerName) =>
      games.get(id) match {
        case Some((gameStats, cells)) =>
          val newStats = gameStats.copy(players = gameStats.players :+ PlayerStats(playerName, Seq.empty, Seq.empty))
          games = games.updated(id, (newStats, cells))
          sender ! GameDesc(id, newStats.toString)
        case None =>
          sender ! GameNotFound
      }
    case GameConnected(gameId, ip, subscriber) =>
      games.get(gameId) match {
        case Some((_, cells)) =>
          subscriber ! GameMapUpdate(cells)
      }


  }
}

object GameMaster {
  def props = Props(classOf[GameMaster])

  sealed trait GameMasterCommand

  case object ListGames extends GameMasterCommand

  sealed trait GameMasterResponse

  sealed trait GameMessage

  case class GameDisconnected(ip: String) extends GameMessage

  case class GameConnected(gameId: String, ip: String, subscriber: ActorRef) extends GameMessage

  case class GameUpdate(packet: Packet) extends GameMessage

}