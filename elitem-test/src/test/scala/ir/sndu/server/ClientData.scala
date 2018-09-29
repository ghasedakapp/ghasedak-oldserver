package ir.sndu.server

import ir.sndu.server.users.ApiUser

case class ClientData(user: ApiUser, token: String, number: Long)

