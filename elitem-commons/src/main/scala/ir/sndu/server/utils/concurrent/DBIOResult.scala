package ir.sndu.server.utils.concurrent

import cats.{ Functor, Monad }
import cats.data.EitherT
import cats.instances.{ EitherInstances, FutureInstances }
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

trait DBIOResult[ErrorCase <: Throwable] extends FutureInstances
  with EitherInstances with ConvertResult[ErrorCase] {

  type Result[A] = EitherT[DBIO, ErrorCase, A]

  def Result[A] = EitherT.apply[DBIO, ErrorCase, A] _

  implicit def dbioFunctor(implicit ec: ExecutionContext) = new Functor[DBIO] with Monad[DBIO] {
    override def pure[A](a: A): DBIO[A] = DBIO.successful(a)
    override def flatMap[A, B](fa: DBIO[A])(f: A ⇒ DBIO[B]): DBIO[B] = fa flatMap f
    override def map[A, B](fa: DBIO[A])(f: A ⇒ B): DBIO[B] = fa map f
    override def tailRecM[A, B](a: A)(f: A ⇒ DBIO[Either[A, B]]): DBIO[B] =
      flatMap(f(a)) {
        case Right(b)    ⇒ pure(b)
        case Left(nextA) ⇒ tailRecM(nextA)(f)
      }
  }

  def point[A](a: A): Result[A] = Result[A](DBIO.successful(Right(a)))

  def fromDBIO[A](fa: DBIO[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fa map (Right(_)))

  def fromOption[A](failure: ErrorCase)(oa: Option[A]): Result[A] = Result[A](DBIO.successful(oa.toRight(failure)))

  def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](DBIO.from(fu.map(Right(_))))

  def fromFutureOption[A](failure: ErrorCase)(fu: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](DBIO.from(fu.map(_ toRight failure)))

  def fromBoolean[A](failure: ErrorCase)(oa: Boolean): Result[Unit] =
    Result[Unit](DBIO.successful(if (oa) Right(()) else Left(failure)))

  def fromDBIOOption[A](failure: ErrorCase)(foa: DBIO[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_ toRight failure))

  def fromDBIOBoolean(failure: ErrorCase)(foa: DBIO[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) Right(()) else Left(failure)))

}