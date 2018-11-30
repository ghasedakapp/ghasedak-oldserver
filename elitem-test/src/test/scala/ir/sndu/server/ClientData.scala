package ir.sndu.server

import ir.sndu.server.apiuser.ApiUser

case class ClientData(user: ApiUser, token: String, number: Long)

