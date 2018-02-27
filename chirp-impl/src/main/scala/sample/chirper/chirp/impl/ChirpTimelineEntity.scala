package sample.chirper.chirp.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

class ChirpTimelineEntity(topic: ChirpTopic) extends PersistentEntity {

  override type Command = ChirpTimelineCommand[_]
  override type Event = ChirpTimelineEvent
  override type State = NotUsed

  override def initialState = NotUsed.getInstance()

  override def behavior =
    Actions()
      .onCommand[AddChirp, Done] {
      case (AddChirp(chirp), ctx, _) =>
        val event = ChirpAdded(chirp)
        ctx.thenPersist(event) { _ =>
          ctx.reply(Done)
          topic.publish(chirp)
        }
    }
      .onEvent {
        case (_, state) =>
          state
      }

}

object ChirpTimelineSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[AddChirp],
    JsonSerializer[ChirpAdded]
  )
}
