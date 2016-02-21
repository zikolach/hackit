package hackit

import scala.util.Random

object Settings {
  val terrainNames = "grass" :: "tree" :: "mountain" :: "sea" :: Nil
  val settlementNames = "village" :: "fort" :: Nil

  def generateTerrain(width: Int, height: Int): Seq[MapCell] = {
    for {
      x <- 0 to width
      y <- 0 to height
      kind = terrainNames(Random.nextInt(terrainNames.size))
    } yield MapCell(x, y, kind)
  }
}
