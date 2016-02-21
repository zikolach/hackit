package hackit

case class PlayerStats(playerName: String,
                       villages: Seq[(Int, Int)],
                       forts: Seq[(Int, Int)])