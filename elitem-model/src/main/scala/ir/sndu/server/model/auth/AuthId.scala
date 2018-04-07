package ir.sndu.server.model.auth

@SerialVersionUID(1L)
case class AuthId(id: String, userId: Option[Int], publicKeyHash: Option[Long])
