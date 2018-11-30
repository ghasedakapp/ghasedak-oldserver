package ir.sndu.server.group

import ir.sndu.server.apigroup.{ ApiGroup, ApiGroupType }
import ir.sndu.server.apipeer.ApiUserOutPeer
import ir.sndu.server.rpcgroups.RequestCreateGroup
import ir.sndu.server.{ ClientData, GrpcBaseSuit }

import scala.util.Random

trait GroupHelper {
  self: GrpcBaseSuit ⇒
  def createGroup(title: String = "Tset Group", members: Seq[ApiUserOutPeer] = Seq.empty)(implicit user: ClientData): ApiGroup = {
    groupStub.createGroup(RequestCreateGroup(
      Random.nextLong(), title, members, ApiGroupType.General)).group.get
  }
}
