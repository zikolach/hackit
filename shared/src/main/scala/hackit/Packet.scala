package hackit


sealed trait Packet

case class BuildVillage(gameId: String, playerName: String, x: Int, y: Int) extends Packet

case class MapCell(x: Int, y: Int, terrain: String)

case class GameMapUpdate(cells: Seq[MapCell]) extends Packet

case class PlayerStatsUpdate(stats: PlayerStats) extends Packet

