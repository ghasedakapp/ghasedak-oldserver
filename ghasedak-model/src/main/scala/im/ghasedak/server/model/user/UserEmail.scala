package im.ghasedak.server.model.user

@SerialVersionUID(1L)
case class UserEmail(
  id:     Int,
  userId: Int,
  email:  String,
  title:  String)
