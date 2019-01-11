package im.ghasedak.server.utils.concurrent

import scala.concurrent.{ ExecutionContext, Future }

trait ConvertResult[ErrorCase <: Throwable] {

  implicit def convertToResponse[B](fa: Future[Either[ErrorCase, B]])(implicit ec: ExecutionContext, onFailure: PartialFunction[Throwable, ErrorCase]) =
    fa flatMap {
      case Right(result) ⇒ Future.successful(result)
      case Left(ex)      ⇒ Future.failed(ex)
    } recoverWith {
      case ex ⇒ Future.failed(onFailure(ex))
    }

}
