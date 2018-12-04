package ir.sndu.server.group

import ir.sndu.api.group._
import ir.sndu.api.peer.ApiUserOutPeer
import ir.sndu.rpc.group.RequestCreateGroup
import ir.sndu.server.{ ClientData, GrpcBaseSuit }

import scala.util.Random

trait GroupHelper {
  self: GrpcBaseSuit ⇒
  def createGroup(title: String = "Tset Group", members: Seq[ApiUserOutPeer] = Seq.empty)(implicit user: ClientData): ApiGroup = {
    groupStub.createGroup(RequestCreateGroup(
      Random.nextLong(), title, members, ApiGroupType.General)).group.get
  }
}
