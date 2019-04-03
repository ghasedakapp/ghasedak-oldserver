package im.ghasedak.server.repo.user

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.api.user._
import im.ghasedak.server.model.user.UserModel
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.utils.number.PhoneNumberUtils
import slick.dbio.Effect
import slick.lifted.{ QueryBase, Tag }
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

import scala.concurrent.ExecutionContext
final class UserTable(tag: Tag) extends Table[UserModel](tag, "users") {
  import SexColumnType._

  def id = column[Int]("id", O.PrimaryKey)

  def orgId = column[Int]("org_id")

  def name = column[String]("name")

  def sex = column[Sex]("sex")

  def createdAt = column[LocalDateTime]("created_at")

  def isBot = column[Boolean]("is_bot")

  def about = column[Option[String]]("about")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def nickname = column[Option[String]]("nickname")

  def countryCode = column[Option[String]]("country_code")

  def * = (id, orgId, name, sex, createdAt, isBot, about, deletedAt, nickname, countryCode) <> (UserModel.tupled, UserModel.unapply)

}

object UserRepo {

  val users = TableQuery[UserTable]

  val active = users.filter(_.deletedAt.isEmpty)

  private def byId(id: Rep[Int]) = users filter (_.id === id)
  private def nameById(id: Rep[Int]) = byId(id) map (_.name)

  val byIdC = Compiled(byId _)
  val nameByIdC = Compiled(nameById _)

  private def byNickname(nickname: Rep[String]) =
    users filter (_.nickname.toLowerCase === nickname.toLowerCase)
  private def byNicknamePrefix(nickPrefix: Rep[String]) =
    users filter (_.nickname.toLowerCase.like(nickPrefix.toLowerCase))

  private val byNicknameC = Compiled(byNickname _)
  private val byNicknamePrefixC = Compiled(byNicknamePrefix _)

  private def byPhone(phone: Rep[Long]) = (for {
    phones ← UserPhoneRepo.phones.filter(_.number === phone)
    users ← users if users.id === phones.userId
  } yield users).take(1)

  private def idByPhone(phone: Rep[Long]) = byPhone(phone) map (_.id)

  val idByPhoneC = Compiled(idByPhone _)

  private def idsByEmail(email: Rep[String]) =
    for {
      emails ← UserEmailRepo.emails filter (_.email.toLowerCase === email.toLowerCase)
      users ← users filter (_.id === emails.userId) map (_.id)
    } yield users
  val idsByEmailC = Compiled(idsByEmail _)

  def create(user: UserModel): FixedSqlAction[Int, NoStream, Effect.Write] =
    users += user

  def find(orgId: Int, id: Int): SqlAction[Option[UserModel], NoStream, Effect.Read] =
    active.filter(_.id === id).result.headOption

  def find(orgId: Int, ids: Set[Int]): FixedSqlStreamingAction[Seq[UserModel], UserModel, Effect.Read] =
    active.filter(_.id inSet ids).result

  def findByNickname(query: String): SqlAction[Option[UserModel], NoStream, Effect.Read] = {
    val nickname =
      if (query.startsWith("@")) query.drop(1) else query
    byNicknameC(nickname).result.headOption
  }

  def findByNicknamePrefix(query: String): FixedSqlStreamingAction[Seq[UserModel], UserModel, Effect.Read] = {
    val nickname: String =
      if (query.startsWith("@")) query.drop(1) else query
    byNicknamePrefixC(nickname).result
  }

  def findIdsByEmail(email: String): SqlAction[Option[Int], NoStream, Effect.Read] =
    idsByEmailC(email).result.headOption

  def findIds(query: String)(implicit ec: ExecutionContext): DBIOAction[Seq[Int], NoStream, Effect.Read with Effect.Read] =
    for {
      e ← idsByEmailC(query).result
      p ← PhoneNumberUtils.normalizeStr(query)
        .headOption
        .map(idByPhoneC(_).result)
        .getOrElse(DBIO.successful(Nil))
    } yield e ++ p

  def findByIds(orgId: Int, ids: Set[Int]): FixedSqlStreamingAction[Seq[UserModel], UserModel, Effect.Read] =
    users.filter(_.orgId === orgId).filter(_.id inSet ids).result

  def isDeleted(userId: Int): DBIO[Boolean] =
    users.filter(_.id === userId).filter(_.deletedAt.nonEmpty).exists.result

  def findOrgId(userId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    active.filter(_.id === userId).map(_.orgId).result.headOption

}

