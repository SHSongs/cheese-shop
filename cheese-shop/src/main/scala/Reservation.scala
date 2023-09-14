case class Reservation(id: Int,
                       user: User,
                       date: String,
                       time: String,
                       guests: String,
                       isClosed: Boolean = false,
                       isPaied: Boolean = false)
