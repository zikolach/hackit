package hackit

case class GameStats(id: String,
                     turn: Int = 0,
                     players: Seq[PlayerStats] = Seq.empty,
                     terrain: Seq[MapCell] = Seq.empty
                    )