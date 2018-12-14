package ir.sndu.server.model.auth

@SerialVersionUID(1L)
case class GateAuthCode(
  transactionHash: String,
  codeHash:        String,
  isDeleted:       Boolean = false)
