package hackit

case class GameStats(id: String, turn: Int = 0, players: Seq[PlayerStats])