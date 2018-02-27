package sample.chirper.friend.impl

import java.time.Instant

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}
import sample.chirper.friend.api.SOSMessage

sealed trait FriendEvent extends AggregateEvent[FriendEvent] {
  override def aggregateTag: AggregateEventTagger[FriendEvent] = FriendEvent.Tag
}

object FriendEvent {
  val Tag = AggregateEventTag[FriendEvent]
}

case class UserCreated(userId: String, name: String, timestamp: Instant = Instant.now()) extends FriendEvent

object UserCreated {
  implicit val format: Format[UserCreated] = Json.format[UserCreated]
}

case class FriendAdded(userId: String, friendId: String, timestamp: Instant = Instant.now()) extends FriendEvent

object FriendAdded {
  implicit val format: Format[FriendAdded] = Json.format[FriendAdded]
}

case class MessageAdded(message: SOSMessage, timestamp: Instant = Instant.now()) extends FriendEvent

object MessageAdded {
  implicit val format: Format[MessageAdded] = Json.format[MessageAdded]
}