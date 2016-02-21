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

  def randomColor = {
    val red = (Random.nextInt(256) + 255) / 2
    val green = (Random.nextInt(256) + 255) / 2
    val blue = (Random.nextInt(256) + 255) / 2
    s"#${Integer.toHexString(red)}${Integer.toHexString(green)}${Integer.toHexString(blue)}"
  }
}
