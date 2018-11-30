package ir.sndu.server.rpc.auth.helper

import java.util.UUID

import akka.event.LoggingAdapter
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.grpc.Context
import ir.sndu.persist.repo.auth.TokenRepo
import ir.sndu.server.model.auth.Token
import ir.sndu.server.rpc.CommonsError._
import ir.sndu.server.rpc.auth.AuthErrors
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

object AuthHelper {

  private val TOKEN_CTX_KEY: Context.Key[String] = Context.key[String]("token")

  case class ClientData(userId: Int, tokenId: String, tokenKey: String, token: String)

  def generateClientData(jwt: DecodedJWT, tokenId: String, tokenKey: String, token: String): ClientData = {
    ClientData(
      Integer.parseInt(jwt.getClaim("userId") asString ()),
      tokenId, tokenKey, token)
  }

}

trait AuthHelper {

  import AuthErrors._
  import AuthHelper._

  implicit val ec: ExecutionContext

  val log: LoggingAdapter

  val db: PostgresProfile.backend.Database

  private def authorize[T](tokenOpt: Option[String])(f: ClientData ⇒ Future[T]): Future[T] = {
    tokenOpt match {
      case None ⇒ Future.failed(MissingToken)
      case Some(token) ⇒
        try {
          val tokenId = JWT.decode(token).getClaim("tokenId").asString()
          db.run(TokenRepo.find(tokenId)) flatMap {
            case None ⇒ Future.failed(InvalidToken)
            case Some(tokenKey) ⇒
              val algorithm = Algorithm.HMAC512(tokenKey)
              val verifier = JWT.require(algorithm).build()
              val jwt = verifier.verify(token)
              val clientData = generateClientData(jwt, tokenId, tokenKey, token)
              f(clientData)
          } recoverWith {
            case _: JWTVerificationException ⇒
              log.warning("invalid token {}", token)
              Future.failed(InvalidToken)
            case ex: Throwable ⇒
              log.error(ex, "Error in authorize token {}", token)
              Future.failed(InternalError)
          }
        } catch {
          case _: JWTVerificationException ⇒
            log.warning("invalid token {}", token)
            Future.failed(InvalidToken)
        }
    }
  }

  def authorize[T](f: ClientData ⇒ Future[T]): Future[T] =
    authorize(Option(TOKEN_CTX_KEY.get()))(f)

  def generateToken(userId: Int): Future[String] = {
    val tokenKey = UUID.randomUUID().toString
    val algorithm = Algorithm.HMAC512(tokenKey)
    val tokenId = UUID.randomUUID().toString + userId
    val tokenStr = JWT.create()
      .withClaim("tokenId", tokenId)
      .withClaim("userId", userId.toString)
      .sign(algorithm)
    val token = Token(tokenId, tokenId, None)
    db.run(TokenRepo.create(token)) map (_ ⇒ tokenStr)
  }

}