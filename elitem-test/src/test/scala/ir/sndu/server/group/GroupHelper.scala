package ir.sndu.server.group

import ir.sndu.server.{ GrpcBaseSuit, UserInfo }
import ir.sndu.server.groups.{ ApiGroup, ApiGroupType }
import ir.sndu.server.peer.ApiUserOutPeer
import ir.sndu.server.rpc.groups.RequestCreateGroup

import scala.util.Random

trait GroupHelper {
  self: GrpcBaseSuit ⇒
  def createGroup(title: String, members: Seq[ApiUserOutPeer])(implicit user: UserInfo): ApiGroup = {
    groupStub.createGroup(RequestCreateGroup(
      Random.nextLong(), title, members, ApiGroupType.General, user.token)).group.get
  }
}
