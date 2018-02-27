package sample.chirper.friend.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRef, PersistentEntityRegistry}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import sample.chirper.friend.api.{FriendId, FriendService, SOSMessage, User}

import scala.concurrent.{ExecutionContext, Future}

class FriendServiceImpl(
                         persistentEntities: PersistentEntityRegistry,
                         db: CassandraSession
                       )(implicit ec: ExecutionContext) extends FriendService {

  override def health() = ServiceCall { _ => Future.successful("OK") }

  override def getUser(userId: String) = ServiceCall { _ =>
    friendEntityRef(userId).ask(GetUser())
      .map(_.user.getOrElse(throw NotFound(s"user $userId not found")))
  }

  override def createUser() = ServiceCall { request =>
    friendEntityRef(request.userId).ask(CreateUser(User(request))).map(_ => NotUsed.getInstance())
  }

  override def addFriend(userId: String): ServiceCall[FriendId, NotUsed] = ServiceCall { request =>
    friendEntityRef(userId).ask(AddFriend(request.friendId)).map(_ => NotUsed.getInstance())
  }

  override def getFollowers(userId: String) = ServiceCall { _ =>
    db.selectAll("SELECT * FROM follower WHERE userId = ?", userId).map { rows =>
      rows.toList.map(_.getString("followedBy"))
    }
  }

  override def saveMessage(rawMessage: String): ServiceCall[NotUsed, SOSMessage] = ServerServiceCall { _ =>
    friendEntityRef("testId").ask(SaveMessage(SOSMessage(rawMessage))) map (x =>
      x
    )
  }

  private def friendEntityRef(userId: String): PersistentEntityRef[FriendCommand[_]] =
    persistentEntities.refFor[FriendEntity](userId)

}
