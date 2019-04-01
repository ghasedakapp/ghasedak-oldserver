package im.ghasedak.server.rpc.user

import im.ghasedak.api.user.UserProfile
import im.ghasedak.server.repo.contact.UserContactRepo
import im.ghasedak.server.repo.user.UserRepo
import im.ghasedak.server.user.UserUtils
import im.ghasedak.server.utils.concurrent.DBIOHelper

import scala.concurrent.Future

trait UserServiceHelper {
  this: UserServiceImpl ⇒

  protected def getUsers(clientOrgId: Int, clientUserId: Int, userIds: Set[Int]): Future[Set[UserProfile]] =
    Future.sequence(userIds.map(uid ⇒ getUser(clientOrgId, clientUserId, uid))).map(_.flatten)

  protected def getUser(clientOrgId: Int, clientUserId: Int, userId: Int): Future[Option[UserProfile]] = {
    val action =
      for {
        userOpt ← UserRepo.find(clientOrgId, userId)
        localNameOpt ← DBIOHelper.fromOption(userOpt.map(u ⇒ UserContactRepo.findName(clientUserId, u.id)))
        records ← UserUtils.getUserContactsRecord(userId)
      } yield userOpt.map { u ⇒
        UserProfile(
          Some(u.toApi(localNameOpt.flatten)),
          records)
      }
    db.run(action)
  }

}
