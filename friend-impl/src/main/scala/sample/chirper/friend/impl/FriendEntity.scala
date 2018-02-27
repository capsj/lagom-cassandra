package sample.chirper.friend.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import sample.chirper.friend.api.{SOSMessage, User}

import scala.collection.mutable.ArrayBuffer

class FriendEntity extends PersistentEntity {

  override type Command = FriendCommand[_]
  override type Event = FriendEvent
  override type State = FriendState

  override def initialState = FriendState(None)

  override def behavior = {
    case FriendState(None) => notInitialized
    case FriendState(Some(user)) => initialized
  }

  val onGetUser = Actions().onReadOnlyCommand[GetUser, GetUserReply] {
    case (GetUser(), ctx, state) => ctx.reply(GetUserReply(state.user))
  }

  val onFriendAdded = Actions().onEvent {
    case (FriendAdded(userId, friendId, timestamp), state) => state.addFriend(friendId)
  }

  val notInitialized = {
    Actions().
      onCommand[CreateUser, Done] {
      case (CreateUser(user), ctx, state) =>
        val events = ArrayBuffer.empty[FriendEvent]
        events += UserCreated(user.userId, user.name)
        events ++= user.friends.map(friendId => FriendAdded(user.userId, friendId))
        ctx.thenPersistAll(events: _*) { () =>
          ctx.reply(Done)
        }
    }.
      onCommand[AddFriend, Done] {
      case (AddFriend(friendUserId), ctx, state) =>
        ctx.invalidCommand(s"User $entityId is not created")
        ctx.done
    }.
      onCommand[SaveMessage, SOSMessage] {
      case (SaveMessage(sOSMessage), ctx, state) =>
        ctx.thenPersist(MessageAdded(sOSMessage)) {x => ctx.reply(x.message)}
    }.
      onEvent {
        case (UserCreated(userId, name, timestamp), state) => FriendState(User(userId, name))
      }.
        onEvent {
          case (MessageAdded(message, timestamp), state) => FriendState(None)
        }
  }.orElse(onGetUser).orElse(onFriendAdded)

  val initialized = {
    Actions().
      onCommand[CreateUser, Done] {
      case (CreateUser(user), ctx, state) =>
        ctx.invalidCommand(s"User ${user.name} is already created")
        ctx.done
    }.
      onCommand[AddFriend, Done] {
      case (AddFriend(friendUserId), ctx, state) if state.user.get.friends.contains(friendUserId) =>
        ctx.reply(Done)
        ctx.done
      case (AddFriend(friendUserId), ctx, state) =>
        val event = FriendAdded(state.user.get.userId, friendUserId)
        ctx.thenPersist(event) { _ =>
          ctx.reply(Done)
        }
    }.
      onCommand[SaveMessage, SOSMessage] {
      case (SaveMessage(sOSMessage), ctx, state) =>
        ctx.thenPersist(MessageAdded(sOSMessage)) {x => ctx.reply(x.message)}
    }.
        onEvent {
          case (MessageAdded(message, timestamp), state) => FriendState(None)
        }
  }.orElse(onGetUser).orElse(onFriendAdded)

}

object FriendSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[GetUser],
    JsonSerializer[GetUserReply],
    JsonSerializer[FriendState],
    JsonSerializer[CreateUser],
    JsonSerializer[UserCreated],
    JsonSerializer[AddFriend],
    JsonSerializer[FriendAdded],

    JsonSerializer[SaveMessage],
    JsonSerializer[MessageAdded]
  )
}
