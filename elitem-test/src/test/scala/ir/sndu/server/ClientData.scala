package ir.sndu.server

import ir.sndu.api.user.ApiUser

case class ClientData(user: ApiUser, token: String, number: Long)

