package sample.chirper.chirp.api

import java.time.Instant

import play.api.libs.json.Json

import scala.collection.immutable.Seq

case class HistoricalChirpsRequest(fromTime: Instant, userIds: Seq[String])

object HistoricalChirpsRequest {
  implicit val format = Json.format[HistoricalChirpsRequest]
}