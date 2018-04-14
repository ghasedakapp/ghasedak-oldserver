package ir.sndu.server

import ir.sndu.server.users.ApiUser

case class UserInfo(user: ApiUser, token: String, number: Long)

