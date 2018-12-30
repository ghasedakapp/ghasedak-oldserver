package ir.sndu.server.model.auth

final case class GateAuthCode(
  transactionHash: String,
  codeHash:        String,
  isDeleted:       Boolean = false)
