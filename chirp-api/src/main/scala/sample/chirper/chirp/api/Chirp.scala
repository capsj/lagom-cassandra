package sample.chirper.chirp.api

import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json
import sample.chirper.chirp.api.Chirp.{defaultTimestamp, defaultUuid}

case class Chirp(userId: String, message: String, timestamp: Instant = defaultTimestamp, uuid: String = defaultUuid)

object Chirp {
  implicit val format = Json.format[Chirp]

  def apply(addChirp: AddChirp): Chirp = Chirp(addChirp.userId, addChirp.message)

  def defaultTimestamp = Instant.now()

  def defaultUuid = UUID.randomUUID().toString

}

case class AddChirp(userId: String, message: String)

object AddChirp {
  implicit val format = Json.format[AddChirp]

  def apply(chirp: Chirp): AddChirp = new AddChirp(chirp.userId, chirp.message)
}