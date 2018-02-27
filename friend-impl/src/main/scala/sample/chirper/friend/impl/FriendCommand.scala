package sample.chirper.friend.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json._
import sample.chirper.friend.api.{SOSMessage, User}

// COMMANDS ------------------------------------------------------------------------------------------------------------

sealed trait FriendCommand[R] extends ReplyType[R]

case class GetUser() extends FriendCommand[GetUserReply]

object GetUser {
  // https://stackoverflow.com/questions/43096129/how-can-i-write-and-read-an-empty-case-class-with-play-json
  implicit val strictReads = Reads[GetUser](json => json.validate[JsObject].filter(_.values.isEmpty).map(_ => GetUser()))
  implicit val writes = OWrites[GetUser](_ => Json.obj())
}

case class CreateUser(user: User) extends FriendCommand[Done]

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format[CreateUser]
}

case class AddFriend(friendUserId: String) extends FriendCommand[Done]

object AddFriend {
  implicit val format: Format[AddFriend] = Json.format[AddFriend]
}

case class SaveMessage(message: SOSMessage) extends FriendCommand[SOSMessage]
object SaveMessage {
  implicit val format: Format[SaveMessage] = Json.format
}


// REPLIES -------------------------------------------------------------------------------------------------------------

case class GetUserReply(user: Option[User])

object GetUserReply {
  implicit val format: Format[GetUserReply] = Json.format[GetUserReply]
}

