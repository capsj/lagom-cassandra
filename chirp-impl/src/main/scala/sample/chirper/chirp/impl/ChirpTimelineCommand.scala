package sample.chirper.chirp.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}
import sample.chirper.chirp.api.Chirp

sealed trait ChirpTimelineCommand[R] extends ReplyType[R]

case class AddChirp(chirp: Chirp) extends ChirpTimelineCommand[Done]

object AddChirp {
  implicit val format: Format[AddChirp] = Json.format[AddChirp]
}