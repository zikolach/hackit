package hackit.rules

import hackit.GameStats

object Rules {

  def ready(game: GameStats): Boolean =
    game.players.forall(_.madeHisTurn)


  def makeTurn(game: GameStats): GameStats = {
    game
  }

  def canBuildVillage(game: GameStats, playerName: String): Boolean =
    game.players.exists { player =>
      player.playerName == playerName && player.wood > 0
    }

  def buildVillage(game: GameStats, playerName: String, x: Int, y: Int): GameStats =
    if (canBuildVillage(game, playerName))
      game.copy(players = game.players.map { player =>
        if (player.playerName == playerName)
          player.copy(villages = player.villages :+(x, y), wood = player.wood - 1)
        else
          player
      })
    else game


}
