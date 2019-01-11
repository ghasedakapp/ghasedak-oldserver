package im.ghasedak.server.model.auth

final case class GateAuthCode(
  transactionHash: String,
  codeHash:        String,
  isDeleted:       Boolean = false)
