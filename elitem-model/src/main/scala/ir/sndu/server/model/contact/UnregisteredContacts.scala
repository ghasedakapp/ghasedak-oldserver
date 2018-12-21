package ir.sndu.server.model.contact

@SerialVersionUID(1L)
case class UnregisteredContact(ownerUserId: Int, localName: String)

@SerialVersionUID(1L)
case class UnregisteredPhoneContact(phoneNumber: Long, ownerUserId: Int, localName: String)

@SerialVersionUID(1L)
case class UnregisteredEmailContact(email: String, ownerUserId: Int, localName: String)
