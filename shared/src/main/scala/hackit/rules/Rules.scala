package hackit.rules

import hackit.{MapCell, GameStats}

object Rules {

  def ready(game: GameStats): Boolean =
    game.players.forall(_.madeHisTurn)


  def makeTurn(game: GameStats): GameStats = {
    game
  }

  def canBuildVillage(game: GameStats, x: Int, y: Int, playerName: String): Boolean =
    game.players.exists { player =>
      val haveResources=player.playerName == playerName && player.wood > 0
      val terrainCheck = game.terrain.exists(tile => tile.x == x && tile.y == y && tile.terrain == "grass")
      haveResources && terrainCheck
    }

  def buildVillage(game: GameStats, playerName: String, x: Int, y: Int): GameStats =
    if (canBuildVillage(game, x, y, playerName))
      game.copy(players = game.players.map { player =>
        if (player.playerName == playerName)
          player.copy(villages = player.villages :+(x, y), wood = player.wood - 1)
        else
          player
      })
    else game



}
