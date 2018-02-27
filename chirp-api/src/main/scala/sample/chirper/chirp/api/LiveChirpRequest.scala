package sample.chirper.chirp.api

import play.api.libs.json.Json

import scala.collection.immutable.Seq

case class LiveChirpRequest(userIds: Seq[String])

object LiveChirpRequest {
  implicit val format = Json.format[LiveChirpRequest]
}
