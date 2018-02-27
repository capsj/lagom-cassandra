package sample.chirper.friend.impl

import play.api.libs.json.{Format, Json}
import sample.chirper.friend.api.User

case class FriendState(user: Option[User]) {

  def addFriend(friendUserId: String): FriendState = user match {
    case None => throw new IllegalStateException("friend can't be added before user is created")
    case Some(user) =>
      val newFriends = user.friends :+ friendUserId
      FriendState(user.copy(friends = newFriends))
  }

}

object FriendState {

  implicit val format: Format[FriendState] = Json.format[FriendState]

  def apply(user: User): FriendState = new FriendState(Some(user))

}