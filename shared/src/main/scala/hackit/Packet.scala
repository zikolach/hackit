package hackit


sealed trait Packet

case class BuildVillage(x: Int, y: Int) extends Packet

case class MapCell(x: Int, y: Int, terrain: String)

case class GameMapUpdate(cells: Seq[MapCell]) extends Packet
