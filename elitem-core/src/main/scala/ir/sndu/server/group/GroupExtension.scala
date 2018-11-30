package ir.sndu.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.group.{ GroupRepo, GroupUserRepo }
import ir.sndu.server.apigroup.{ ApiGroup, ApiGroupType }
import ir.sndu.server.model.group.Group
import ir.sndu.server.sequencestruct.SeqState
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

class GroupExtensionImpl(system: ActorSystem) extends Extension {
  implicit val ec: ExecutionContext = system.dispatcher
  val db: PostgresProfile.backend.Database = PostgresDb.db

  def create(
    typ:           ApiGroupType,
    creatorUserId: Int,
    title:         String,
    randomId:      Long,
    userIds:       Seq[Int]): Future[ApiGroup] = {

    val groupId = Random.nextInt()

    val creationResult = for {
      _ ← GroupRepo.create(Group(
        id = groupId,
        creatorUserId = creatorUserId,
        accessHash = Random.nextLong(),
        title = title,
        createdAt = LocalDateTime.now(ZoneOffset.UTC),
        typ = typ,
        about = None,
        topic = None), randomId)
      _ ← GroupUserRepo.create(
        groupId = groupId,
        userId = creatorUserId,
        inviterUserId = creatorUserId,
        invitedAt = LocalDateTime.now(ZoneOffset.UTC),
        joinedAt = None,
        isAdmin = true)

    } yield ()

    for {
      _ ← db.run(creationResult)
      _ ← Future.sequence(userIds map (userId ⇒ invite(
        groupId,
        inviteeUserId = userId,
        inviterUserId = creatorUserId)))

    } yield ApiGroup(
      id = groupId,
      title = title,
      groupType = typ)

  }

  def invite(
    groupId:       Int,
    inviteeUserId: Int,
    inviterUserId: Int): Future[SeqState] = {
    db.run(GroupUserRepo.create(
      groupId = groupId,
      userId = inviteeUserId,
      inviterUserId = inviterUserId,
      invitedAt = LocalDateTime.now(ZoneOffset.UTC),
      joinedAt = None,
      isAdmin = false)) map (_ ⇒ SeqState())
  }

  def kick(
    groupId: Int,
    userId:  Int): Future[SeqState] = {
    //TODO make orphan if owner was deleted
    db.run(GroupUserRepo.delete(groupId, userId)) map (_ ⇒ SeqState())
  }

}

object GroupExtension extends ExtensionId[GroupExtensionImpl] {
  override def createExtension(system: ExtendedActorSystem): GroupExtensionImpl = new GroupExtensionImpl(system)
}

