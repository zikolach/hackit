package hackit

import scala.util.Random

object Settings {
  private val grass = "grass"
  private val tree = "tree"
  private val mountain = "mountain"
  private val sea = "sea"
  val terrainNames = grass :: tree :: mountain :: sea :: Nil
  val settlementNames = "village" :: "fort" :: Nil

  def generateTerrain(width: Int, height: Int): Seq[MapCell] = {
    for {
      x <- 0 until width
      y <- 0 until height
      kind = terrainNames(Random.nextInt(terrainNames.size))
    } yield MapCell(x, y, kind)
  }
}
