package ir.sndu.server.utils.concurrent

import cats.data.EitherT
import cats.instances.{ EitherInstances, FutureInstances }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

trait FutureResult[ErrorCase] extends FutureInstances with EitherInstances {

  type Result[A] = EitherT[Future, ErrorCase, A]

  def Result[A] = EitherT.apply[Future, ErrorCase, A] _

  def point[A](a: A): Result[A] = Result[A](Future.successful(Right(a)))

  def fromXor[A](va: ErrorCase Either A): Result[A] = Result[A](Future.successful(va))

  def fromXor[A, B](errorHandle: B ⇒ ErrorCase)(va: B Either A): Result[A] = Result[A](Future.successful(va.left.map(errorHandle)))

  def fromOption[A](failure: ErrorCase)(oa: Option[A]): Result[A] = Result[A](Future.successful(oa.toRight(failure)))

  def fromBoolean[A](failure: ErrorCase)(oa: Boolean): Result[Unit] = Result[Unit](Future.successful(if (oa) Right() else Left(failure)))

  def fromFuture[A](fa: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fa.map(Right(_)))

  def fromFuture[A](errorHandle: Throwable ⇒ ErrorCase)(fu: Future[A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fu.map(Right(_)) recover { case e ⇒ Left(errorHandle(e)) })

  def fromFutureXor[A](fva: Future[ErrorCase Either A])(implicit ec: ExecutionContext): Result[A] = Result[A](fva)

  def fromFutureXor[A](errorHandle: Throwable ⇒ ErrorCase)(fea: Future[Throwable Either A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fea map (either ⇒ either.left.map(errorHandle)))

  def fromFutureOption[A](failure: ErrorCase)(foa: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_.toRight(failure)))

  def fromFutureBoolean(failure: ErrorCase)(foa: Future[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) Right(()) else Left(failure)))
}