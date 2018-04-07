package ir.sndu.persist.repo

import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.server.model.user.{ Sex, User, UserState }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import TypeMapper._

final class UserTable(tag: Tag) extends Table[User](tag, "users") {
  import SexColumnType._
  import UserStateColumnType._

  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def name = column[String]("name")
  def countryCode = column[String]("country_code")
  def sex = column[Sex]("sex")
  def state = column[UserState]("state")
  def createdAt = column[LocalDateTime]("created_at")
  def nickname = column[Option[String]]("nickname")
  def about = column[Option[String]]("about")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")
  def isBot = column[Boolean]("is_bot")

  def * = (id, accessSalt, name, countryCode, sex, state, createdAt, nickname, about, deletedAt, isBot) <> (User.tupled, User.unapply)
}

object UserRepo {
  val users = TableQuery[UserTable]

  def byId(id: Rep[Int]) = users filter (_.id === id)
  def nameById(id: Rep[Int]) = byId(id) map (_.name)

  val byIdC = Compiled(byId _)
  val nameByIdC = Compiled(nameById _)

  private def byNickname(nickname: Rep[String]) =
    users filter (_.nickname.toLowerCase === nickname.toLowerCase)
  private def byNicknamePrefix(nickPrefix: Rep[String]) =
    users filter (_.nickname.toLowerCase.like(nickPrefix.toLowerCase))

  private val byNicknameC = Compiled(byNickname _)
  private val byNicknamePrefixC = Compiled(byNicknamePrefix _)

  def byPhone(phone: Rep[Long]) = (for {
    phones ← UserPhoneRepo.phones.filter(_.number === phone)
    users ← users if users.id === phones.userId
  } yield users).take(1)
  def idByPhone(phone: Rep[Long]) = byPhone(phone) map (_.id)

  val idByPhoneC = Compiled(idByPhone _)

  private val activeHumanUsers =
    users.filter(u ⇒ u.deletedAt.isEmpty && !u.isBot)

  private val activeHumanUsersC = Compiled(activeHumanUsers)

  private val activeHumanUsersIdsC = Compiled(activeHumanUsers map (_.id))

  private def activeHumanUsersIds(createdAfter: Rep[LocalDateTime]) =
    Compiled {
      users.filter(u ⇒ u.deletedAt.isEmpty && !u.isBot && u.createdAt > createdAfter).sortBy(_.createdAt.asc).map(u ⇒ u.id → u.createdAt)
    }

  def activeUserIdsCreatedAfter(createdAfter: LocalDateTime): DBIO[Seq[(Int, LocalDateTime)]] = activeHumanUsersIds(createdAfter).result

  def fetchPeople = activeHumanUsersC.result

  def create(user: User) =
    users += user

  def setCountryCode(userId: Int, countryCode: String) =
    users.filter(_.id === userId).map(_.countryCode).update(countryCode)

  def setDeletedAt(userId: Int) =
    users.filter(_.id === userId).
      map(_.deletedAt).
      update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def setName(userId: Int, name: String) =
    users.filter(_.id === userId).map(_.name).update(name)

  def allIds = users.map(_.id).result

  def all = users.result

  def find(id: Int) =
    byIdC(id).result.headOption

  @deprecated("Duplicates ", "2016-07-07")
  def findName(id: Int) =
    nameById(id).result.headOption

  // TODO: #perf will it create prepared statement for each ids length?
  def findSalts(ids: Set[Int]) =
    users.filter(_.id inSet ids).map(u ⇒ (u.id, u.accessSalt)).result

  @deprecated("user GlobalNamesStorageKeyValueStorage instead", "2016-07-17")
  def findByNickname(query: String): DBIO[Option[User]] = {
    val nickname =
      if (query.startsWith("@")) query.drop(1) else query
    byNicknameC(nickname).result.headOption
  }

  @deprecated("user GlobalNamesStorageKeyValueStorage instead", "2016-07-17")
  def findByNicknamePrefix(query: String): DBIO[Seq[User]] = {
    val nickname: String =
      if (query.startsWith("@")) query.drop(1) else query
    byNicknamePrefixC(nickname).result
  }

  @deprecated("user GlobalNamesStorageKeyValueStorage instead", "2016-07-17")
  def setNickname(userId: Int, nickname: Option[String]) =
    byId(userId).map(_.nickname).update(nickname)

  def setAbout(userId: Int, about: Option[String]) =
    byId(userId).map(_.about).update(about)

  @deprecated("user GlobalNamesStorageKeyValueStorage instead", "2016-07-17")
  def nicknameExists(nickname: String) =
    users.filter(_.nickname.toLowerCase === nickname.toLowerCase).exists.result

  def findByIds(ids: Set[Int]) =
    users.filter(_.id inSet ids).result

  def findByIdsPaged(ids: Set[Int], number: Int, size: Int) = {
    val offset = (number - 1) * size
    users.
      filter(_.id inSet ids).
      sortBy(_.name).
      drop(offset).
      take(size).
      result
  }

  def activeUsersIds = activeHumanUsersIdsC.result

  def isDeleted(userId: Int): DBIO[Boolean] =
    byIdC.applied(userId).filter(_.deletedAt.nonEmpty).exists.result
}
