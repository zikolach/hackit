package hackit

case class PlayerStats(color: String,
                       villages: Seq[(Int, Int)],
                       forts: Seq[(Int, Int)])