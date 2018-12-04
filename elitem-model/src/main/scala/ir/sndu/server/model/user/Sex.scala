package ir.sndu.server.model.user

sealed trait Sex {
  def toOption: Option[Sex]
  def toInt: Int
}

object Sex {
  @SerialVersionUID(1L)
  case object NoSex extends Sex {
    val toOption: None.type = None
    val toInt = 0
  }

  @SerialVersionUID(1L)
  case object Male extends Sex {
    val toOption = Some(Male)
    val toInt = 1
  }

  @SerialVersionUID(1L)
  case object Female extends Sex {
    val toOption = Some(Female)
    val toInt = 2
  }

  def fromInt(i: Int): Sex = i match {
    case 0 ⇒ NoSex
    case 1 ⇒ Male
    case 2 ⇒ Female
  }
}
