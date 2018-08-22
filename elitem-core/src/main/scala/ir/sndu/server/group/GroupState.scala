package ir.sndu.server.group

import java.time.Instant

import ir.sndu.server.file.Avatar
import ir.sndu.server.group.GroupEvents.Created

private[group] object GroupState {
  def empty: GroupState =
    GroupState(
      id = 0,
      createdAt = None,
      creatorUserId = 0,
      ownerUserId = 0,
      exUserIds = Set.empty,
      title = "",
      about = None,
      avatar = None,
      topic = None,
      shortName = None,
      groupType = GroupType.General,
      isHidden = false,
      isHistoryShared = false,
      isAsyncMembers = false,
      members = Map.empty,
      invitedUserIds = Set.empty,
      accessHash = 0L,
      adminSettings = AdminSettings.PlainDefault,
      bot = None,
      deletedAt = None,
      exts = Seq.empty)
}

private[group] final case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     Instant,
  isAdmin:       Boolean // TODO: remove, use separate admins list instead
)

private[group] final case class Bot(
  userId: Int,
  token:  String)

private[group] final case class AdminSettings(
  showAdminsToMembers:     Boolean, // 1
  canMembersInvite:        Boolean, // 2
  canMembersEditGroupInfo: Boolean, // 4
  canAdminsEditGroupInfo:  Boolean, // 8
  showJoinLeaveMessages:   Boolean // 16
)
object AdminSettings {
  val PlainDefault = AdminSettings(
    showAdminsToMembers = true,
    canMembersInvite = true,
    canMembersEditGroupInfo = true,
    canAdminsEditGroupInfo = true,
    showJoinLeaveMessages = true)

  val ChannelsDefault = AdminSettings(
    showAdminsToMembers = false,
    canMembersInvite = false,
    canMembersEditGroupInfo = false,
    canAdminsEditGroupInfo = true,
    showJoinLeaveMessages = false)

//  // format: OFF
//  def apiToBitMask(settings: ApiAdminSettings): Int = {
//    def toInt(b: Boolean) = if (b) 1 else 0
//
//    (toInt(settings.showAdminsToMembers)     << 0) +
//      (toInt(settings.canMembersInvite)        << 1) +
//      (toInt(settings.canMembersEditGroupInfo) << 2) +
//      (toInt(settings.canAdminsEditGroupInfo)  << 3) +
//      (toInt(settings.showJoinLeaveMessages)   << 4)
//  }

  def fromBitMask(mask: Int): AdminSettings = {
    AdminSettings(
      showAdminsToMembers     = (mask & (1 << 0)) != 0,
      canMembersInvite        = (mask & (1 << 1)) != 0,
      canMembersEditGroupInfo = (mask & (1 << 2)) != 0,
      canAdminsEditGroupInfo  = (mask & (1 << 3)) != 0,
      showJoinLeaveMessages   = (mask & (1 << 4)) != 0
    )
  }
  // format: ON
}
private[group] final case class GroupState(
  // creation/ownership
  id:            Int,
  createdAt:     Option[Instant],
  creatorUserId: Int,
  ownerUserId:   Int,
  exUserIds:     Set[Int],

  // group summary info
  title:           String,
  about:           Option[String],
  avatar:          Option[Avatar],
  topic:           Option[String],
  shortName:       Option[String],
  groupType:       GroupType,
  isHidden:        Boolean,
  isHistoryShared: Boolean,
  isAsyncMembers:  Boolean,

  // members info
  members:        Map[Int, Member],
  invitedUserIds: Set[Int],

  //security and etc.
  accessHash:    Long,
  adminSettings: AdminSettings,
  bot:           Option[Bot],
  deletedAt:     Option[Instant],
  exts:          Seq[GroupExt]) {
  def update: PartialFunction[GroupEvent, GroupState] = {
    case evt: Created ⇒
      this.copy(
        id = evt.groupId,
        createdAt = Some(evt.ts),
        creatorUserId = evt.creatorUserId,
        ownerUserId = evt.creatorUserId,
        title = evt.title,
        about = None,
        avatar = None,
        topic = None,
        shortName = None,
        groupType = evt.typ,
        members = (
          evt.userIds map { userId ⇒
            userId →
              Member(
                userId,
                evt.creatorUserId,
                evt.ts,
                isAdmin = userId == evt.creatorUserId)
          }).toMap,
        invitedUserIds = evt.userIds.filterNot(_ == evt.creatorUserId).toSet,
        accessHash = evt.accessHash,
        adminSettings =
          if (evt.typ.isChannel) AdminSettings.ChannelsDefault
          else AdminSettings.PlainDefault,
        bot = None)
    case _ ⇒ this
  }
}
