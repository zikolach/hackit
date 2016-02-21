package hackit.actors

import akka.actor.{ActorRef, Actor, Props}
import hackit.actors.GameMaster.{GameUpdate, GameConnected, ListGames}
import hackit.commands.{GameNotFound, JoinGame, CreateGame}
import hackit._

class GameMaster extends Actor {

  var games: Map[String, (GameStats, Seq[MapCell])] = Map.empty
  var subscribers: Map[String, Seq[ActorRef]] = Map.empty

  import Settings._

  def receive: Receive = {
    case ListGames =>
      sender ! GameList(games.values.map({ case (game, _) => GameDesc(game.id, game.toString) }).toSeq)
    case CreateGame(id, playerName) =>
      val gameStats = GameStats(id, 0, Seq(PlayerStats(playerName, Seq.empty, Seq.empty)))
      games = games + (id ->(gameStats, generateTerrain(10, 10)))
      subscribers = subscribers.updated(id, Seq.empty)
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
          subscribers = subscribers.updated(gameId, subscribers(gameId) :+ subscriber)
          subscriber ! GameMapUpdate(cells)
        case _ =>
      }
    case GameUpdate(ip, BuildVillage(gameId, playerName, x, y)) =>
      games.get(gameId) match {
        case Some((game, cells)) =>
          games = games.updated(gameId, (game.copy(players = game.players.map {
            case player if player.playerName == playerName => player.copy(villages = player.villages :+(x, y))
            case player => player
          }), cells))
          subscribers(gameId).foreach {
            _ ! VillageBuilt(x, y)
          }
          println(s"Build village at $x:$y")
        case _ =>
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

  case class GameUpdate(ip: String, packet: Packet) extends GameMessage

}