package im.ghasedak.server.rpc.auth.helper

import java.util.UUID

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.grpc.GrpcServiceException
import akka.stream.scaladsl.Source
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import im.ghasedak.server.model.auth.AuthToken
import im.ghasedak.server.repo.auth.AuthTokenRepo
import im.ghasedak.server.rpc.Constant
import im.ghasedak.server.rpc.auth.AuthRpcErrors
import im.ghasedak.server.rpc.common.CommonRpcErrors._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

object AuthTokenHelper {

  case class ClientData(userId: Int, orgId: Int, tokenId: String, tokenKey: String, token: String)

  def generateClientData(jwt: DecodedJWT, tokenId: String, tokenKey: String, token: String): ClientData = {
    ClientData(
      Integer.parseInt(jwt.getClaim("userId") asString ()),
      Integer.parseInt(jwt.getClaim("orgId") asString ()),
      tokenId, tokenKey, token)
  }

}

trait AuthTokenHelper {

  import AuthRpcErrors._
  import AuthTokenHelper._

  implicit val ec: ExecutionContext

  val log: LoggingAdapter

  val db: PostgresProfile.backend.Database

  private def authorize[T](tokenOpt: Option[String])(f: ClientData ⇒ Future[T]): Future[T] = {
    tokenOpt match {
      case None ⇒ Future.failed(MissingToken)
      case Some(token) ⇒
        try {
          val tokenId = JWT.decode(token).getClaim("tokenId").asString()
          // todo: use cache
          db.run(AuthTokenRepo.find(tokenId)) flatMap {
            case None ⇒ Future.failed(InvalidToken)
            case Some(tokenKey) ⇒
              val algorithm = Algorithm.HMAC512(tokenKey)
              val verifier = JWT.require(algorithm).build()
              val jwt = verifier.verify(token)
              val clientData = generateClientData(jwt, tokenId, tokenKey, token)
              f(clientData)
          } recoverWith {
            case ex: JWTVerificationException ⇒
              log.error(ex, "invalid token {}", token)
              Future.failed(InvalidToken)
            case ex: GrpcServiceException ⇒
              log.warning(ex.getMessage)
              Future.failed(ex)
            case ex: Throwable ⇒
              log.error(ex, "Error in handling request")
              Future.failed(InternalError)
          }
        } catch {
          case _: JWTVerificationException ⇒
            log.warning("invalid token {}", token)
            Future.failed(InvalidToken)
        }
    }
  }

  import akka.grpc.scaladsl.Metadata

  def authorize[T](metadata: Metadata)(f: ClientData ⇒ Future[T]): Future[T] =
    authorize(metadata.getText(Constant.tokenMetadataKey))(f)

  def authorizeStream[T, M](metadata: Metadata)(f: ClientData ⇒ Source[T, M]): Source[T, NotUsed] =
    Source.fromFutureSource(authorize(metadata)(f.andThen(Future.successful)))
      .mapMaterializedValue(_ ⇒ NotUsed)

  def authorizeFutureStream[T, M](metadata: Metadata)(f: ClientData ⇒ Future[Source[T, M]]): Source[T, NotUsed] =
    Source.fromFutureSource(authorize(metadata)(f))
      .mapMaterializedValue(_ ⇒ NotUsed)

  def generateToken(userId: Int, orgId: Int): Future[(String, String)] = {
    val tokenKey = UUID.randomUUID().toString
    val algorithm = Algorithm.HMAC512(tokenKey)
    val tokenId = UUID.randomUUID().toString + userId
    val tokenStr = JWT.create()
      .withClaim("tokenId", tokenId)
      .withClaim("userId", userId.toString)
      .withClaim("orgId", orgId.toString)
      .sign(algorithm)
    val token = AuthToken(tokenId, tokenKey, None)
    db.run(AuthTokenRepo.create(token)) map (_ ⇒ (tokenId, tokenStr))
  }

}