package sample.chirper.friend.api

import play.api.libs.json.{Format, Json}

case class FriendId(friendId: String)

object FriendId {

  implicit val format: Format[FriendId] = Json.format[FriendId]

}