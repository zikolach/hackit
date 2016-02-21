package hackit.rules

import hackit.{MapCell, PlayerStats, GameStats}

import scala.util.Random

object Rules {

  def ready(game: GameStats): Boolean =
    game.players.forall(_.madeHisTurn)


  def neighbourTerrains(terrain: Seq[MapCell], village: (Int, Int)): Seq[String] = {
    val rels = if (village._1 % 2 == 0)
      Set((0, -1), (0, +1), (-1, 0), (+1, 0), (-1, +1), (+1, +1))
    else
      Set((0, -1), (0, +1), (-1, 0), (+1, 0), (-1, -1), (+1, -1))
    val neighbours = rels.map(rel => (rel._1 + village._1, rel._2 + village._2))
    val cells = terrain.filter(tile => neighbours.contains((tile.x, tile.y)))
    cells.map(_.terrain)
  }

  def gatherResources(game: GameStats, player: PlayerStats): (Int, Int, Int) = {
    player.villages.flatMap(village => {
      val villageNeighbours: Seq[String] = neighbourTerrains(game.terrain, village)
      villageNeighbours map {
        case "grass" => (0, 0, 0)
        case "tree" => (0, 1, 0)
        case "sea" => (1, 0, 0)
        case "mountain" => (0, 0, 1)
      }
    }).foldLeft((0, 0, 0))((acc: (Int, Int, Int), res: (Int, Int, Int)) =>
      (acc._1 + res._1, acc._2 + res._2, acc._3 + res._3))
  }

  def makeTurn(game: GameStats): GameStats = {
    if (ready(game)) {
      game.copy(
        turn = game.turn + 1,
        players = game.players.map(player => {
          val (food, wood, stone) = gatherResources(game, player)
          player.copy(
            madeHisTurn = false,
            food = player.food + food,
            wood = player.wood + wood,
            stone = player.stone + stone
          )
        })
      )
    } else {
      game
    }
  }

  private val villageWoodCost = 5

  def canBuildVillage(game: GameStats, x: Int, y: Int, playerName: String): Boolean =
    game.players.exists { player =>
      val haveResources = player.playerName == playerName && player.wood >= villageWoodCost
      val terrainCheck = game.terrain.exists(tile => tile.x == x && tile.y == y && tile.terrain == "grass")
      val structureCheck = !game.players.exists(p => p.villages.contains((x, y)) || p.forts.contains((x, y)))
      haveResources && terrainCheck && !player.madeHisTurn && structureCheck
    }

  def buildVillage(game: GameStats, playerName: String, x: Int, y: Int): GameStats =
    if (canBuildVillage(game, x, y, playerName))
      game.copy(players = game.players.map { player =>
        if (player.playerName == playerName)
          player.copy(villages = player.villages :+(x, y), wood = player.wood - villageWoodCost, madeHisTurn = true)
        else
          player
      })
    else game


}
