package im.ghasedak.server.model.auth

final case class GateAuthCode(
  transactionHash: String,
  codeHash:        String,
  attempts:        Int     = 0,
  isDeleted:       Boolean = false)
