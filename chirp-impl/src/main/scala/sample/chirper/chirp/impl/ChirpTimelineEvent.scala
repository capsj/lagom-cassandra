package sample.chirper.chirp.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}
import sample.chirper.chirp.api.Chirp

sealed trait ChirpTimelineEvent extends AggregateEvent[ChirpTimelineEvent] {
  override def aggregateTag: AggregateEventShards[ChirpTimelineEvent] = ChirpTimelineEvent.Tag
}

object ChirpTimelineEvent {
  val NumShards = 3
  val Tag = AggregateEventTag.sharded[ChirpTimelineEvent](NumShards)
}

case class ChirpAdded(chirp: Chirp) extends ChirpTimelineEvent

object ChirpAdded {
  implicit val format: Format[ChirpAdded] = Json.format[ChirpAdded]
}
