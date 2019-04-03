package im.ghasedak.server.utils.concurrent

import slick.dbio.DBIO

object DBIOHelper {
  import scala.concurrent.ExecutionContext

  def fromOption[A](x: Option[DBIO[A]])(implicit ec: ExecutionContext): DBIO[Option[A]] =
    x match {
      case Some(f) ⇒ f.map(Some(_))
      case None    ⇒ DBIO.successful(None)
    }
}
