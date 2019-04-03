package im.ghasedak.server.utils.concurrent

object FutureHelper {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext

  def fromOption[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some(f) ⇒ f.map(Some(_))
      case None    ⇒ Future.successful(None)
    }
}
