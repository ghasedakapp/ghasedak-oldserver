package ir.sndu.server.rpc.auth

import akka.event.LoggingAdapter
import ir.sndu.persist.repo.AuthIdRepo
import ir.sndu.server.rpc.{ CommonsError, RpcError }
import slick.jdbc.PostgresProfile._

import scala.concurrent.{ ExecutionContext, Future }

trait AuthHelper {
  def authorize[T](token: String)(f: Int => Future[T])(
    implicit
    db: backend.Database,
    ec: ExecutionContext,
    log: LoggingAdapter): Future[T] = {
    db.run(AuthIdRepo.find(token)) flatMap {
      case Some(auth) => f(auth.userId.get)
      case None =>
        log.warning("Invalid token: {}", token)
        Future.failed(AuthErrors.InvalidToken)
    } recoverWith {
      case e: Throwable if !e.isInstanceOf[RpcError] =>
        log.error(e, "Errors in reading from database")
        Future.failed(CommonsError.InternalError)
    }
  }
}