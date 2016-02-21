package hackit

case class PlayerStats(playerName: String,
                       villages: Seq[(Int, Int)],
                       forts: Seq[(Int, Int)],
                       madeHisTurn: Boolean = false,
                       food: Int = 10,
                       wood: Int = 10,
                       stone: Int = 10
                      )