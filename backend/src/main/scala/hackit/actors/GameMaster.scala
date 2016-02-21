package hackit.actors

import akka.actor.{ActorRef, Actor, Props}
import hackit.actors.GameMaster.{GameDisconnected, GameUpdate, GameConnected, ListGames}
import hackit.commands.{GameNotFound, JoinGame, CreateGame}
import hackit._
import hackit.rules.Rules

class GameMaster extends Actor {

  var games: Map[String, GameStats] = Map.empty
  var subscribers: Map[String, Seq[ActorRef]] = Map.empty

  import Settings._
  import Rules._

  def receive: Receive = {
    case ListGames =>
      sender ! GameList(games.values.map({ case game => GameDesc(game.id, game.toString) }).toSeq)
    case CreateGame(id, playerName) =>
      val gameStats = GameStats(id, 0, Seq(PlayerStats(playerName, Seq.empty, Seq.empty)), generateTerrain(10, 10))
      games = games + (id ->gameStats)
      subscribers = subscribers.updated(id, Seq.empty)
      sender ! GameDesc(gameStats.id, gameStats.toString)
    case JoinGame(id, playerName) =>
      games.get(id) match {
        case Some(gameStats) =>
          val newStats = gameStats.copy(players = gameStats.players :+ PlayerStats(playerName, Seq.empty, Seq.empty))
          games = games.updated(id, newStats)
          sender ! GameDesc(id, newStats.toString)
        case None =>
          sender ! GameNotFound
      }
    case GameConnected(gameId, ip, subscriber) =>
      games.get(gameId) match {
        case Some(stats) =>
          subscribers = subscribers.updated(gameId, subscribers(gameId) :+ subscriber)
          subscriber ! GameMapUpdate(stats.terrain)
          stats.players.foreach { playerStats =>
            subscribers(gameId).foreach {
              _ ! PlayerStatsUpdate(playerStats)
            }
          }
        case _ =>
      }

    case GameDisconnected(ip) =>

    case GameUpdate(ip, BuildVillage(gameId, playerName, x, y)) =>
      games.get(gameId) match {
        case Some(game) =>
          if (canBuildVillage(game, x, y, playerName)) {
            val gameStats = makeTurn(buildVillage(game, playerName, x, y))
            games = games.updated(gameId, gameStats)
            gameStats.players.foreach { playerStats =>
              subscribers(gameId).foreach {
                _ ! PlayerStatsUpdate(playerStats)
              }
            }
          }
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