package sample.chirper.chirp.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

trait ChirpService extends Service {

  def health(): ServiceCall[NotUsed, String]

  def addChirp(userId: String): ServiceCall[AddChirp, NotUsed]

  def getLiveChirps(): ServiceCall[LiveChirpRequest, Source[Chirp, NotUsed]]

  def getHistoricalChirps(): ServiceCall[HistoricalChirpsRequest, Source[Chirp, NotUsed]]

  override def descriptor = named("chirpservice").withCalls(
    pathCall("/api/chirps/health", health _),
    pathCall("/api/chirps/live/:userId", addChirp _),
    namedCall("/api/chirps/live", getLiveChirps _),
    namedCall("/api/chirps/history", getHistoricalChirps _)
  ).withAutoAcl(true)
}
