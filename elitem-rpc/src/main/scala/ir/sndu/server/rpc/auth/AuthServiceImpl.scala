package ir.sndu.server.rpc.auth

import ir.sndu.server.auth.AuthServiceGrpc.AuthService
import ir.sndu.server.auth.{ RequestStartPhoneAuth, ResponseStartPhoneAuth }

import scala.concurrent.Future

class AuthServiceImpl extends AuthService {
  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    println("salam")
    Future.successful(ResponseStartPhoneAuth())
  }
}
