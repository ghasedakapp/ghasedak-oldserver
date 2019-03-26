package im.ghasedak.server.model.user

@SerialVersionUID(1L)
case class UserPhone(
  id:     Int,
  userId: Int,
  number: Long,
  title:  String)
