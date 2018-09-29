package ir.sndu.server.group

import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.group.GroupRepo
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.groups.ApiGroupOutPeer
import ir.sndu.server.misc.ResponseSeqDate
import ir.sndu.server.model.group.Group
import ir.sndu.server.peer.ApiUserOutPeer
import ir.sndu.server.rpc.groups.{ RequestInviteUser, RequestKickUser }
import ir.sndu.server.{ GrpcBaseSuit, ClientData }

import scala.util.Random

class GroupSpec extends GrpcBaseSuit
  with AuthHelper
  with GroupHelper {
  behavior of "GroupService"

  it should "create group" in create
  it should "invite member to group" in invite
  it should "kick member from group" in kick

  def create: Unit = {
    val clientData = createUser()
    val ClientData(user1, token1, _) = clientData

    val apiGroup = {
      implicit val client = clientData
      createGroup("Fun Group", Seq.empty)
    }

    whenReady(PostgresDb.db.run(GroupRepo.find(apiGroup.id))) { group ⇒
      inside(group) {
        case Some(Group(id, creator, _, title, _, typ, _, _)) ⇒
          id shouldEqual apiGroup.id
          title shouldEqual apiGroup.title
          creator shouldEqual user1.id
          typ shouldEqual apiGroup.groupType
      }
    }
  }

  def invite: Unit = {
    val clientData1 = createUser()
    val ClientData(user1, token1, _) = clientData1
    val clientData2 = createUser()
    val ClientData(user2, token2, _) = clientData2

    val apiGroup = {
      implicit val client = clientData1
      createGroup("Fun Group", Seq.empty)
    }

    groupStub.inviteUser(RequestInviteUser(
      Some(ApiGroupOutPeer(apiGroup.id)),
      Some(ApiUserOutPeer(user2.id)),
      Random.nextLong(),
      token1)) shouldBe ResponseSeqDate()
  }

  def kick: Unit = {
    val clientData1 = createUser()
    val ClientData(user1, token1, _) = clientData1
    val clientData2 = createUser()
    val ClientData(user2, token2, _) = clientData2

    val apiGroup = {
      implicit val client = clientData1
      createGroup("Fun Group", Seq.empty)
    }

    groupStub.inviteUser(RequestInviteUser(
      Some(ApiGroupOutPeer(apiGroup.id)),
      Some(ApiUserOutPeer(user2.id)),
      Random.nextLong(),
      token1))

    groupStub.kickUser(RequestKickUser(
      Some(ApiGroupOutPeer(apiGroup.id)),
      Some(ApiUserOutPeer(user2.id)),
      Random.nextLong(),
      token1)) shouldBe ResponseSeqDate()

  }
}
