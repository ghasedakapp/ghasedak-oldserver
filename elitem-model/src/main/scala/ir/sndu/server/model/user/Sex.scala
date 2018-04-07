package ir.sndu.server.model.user

sealed trait Sex {
  def toOption: Option[Sex]
  def toInt: Int
}

object Sex {
  @SerialVersionUID(1L)
  case object NoSex extends Sex {
    val toOption = None
    val toInt = 1
  }

  @SerialVersionUID(1L)
  case object Male extends Sex {
    val toOption = Some(Male)
    val toInt = 2
  }

  @SerialVersionUID(1L)
  case object Female extends Sex {
    val toOption = Some(Female)
    val toInt = 3
  }
  def fromInt(i: Int): Sex = i match {
    case 1 ⇒ NoSex
    case 2 ⇒ Male
    case 3 ⇒ Female
  }
}

class SexE extends Enumeration {

}