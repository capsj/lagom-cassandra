package sample.chirper.activity.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import sample.chirper.chirp.api.Chirp

trait ActivityStreamService extends Service {

  def health(): ServiceCall[NotUsed, String]

  def getLiveActivityStream(userId: String): ServiceCall[NotUsed, Source[Chirp, NotUsed]]

  def getHistoricalActivityStream(userId: String): ServiceCall[NotUsed, Source[Chirp, NotUsed]]

  override def descriptor = named("activityservice").withCalls(
    pathCall("/api/activity/health", health _),
    pathCall("/api/activity/:userId/live", getLiveActivityStream _),
    pathCall("/api/activity/:userId/history", getHistoricalActivityStream _)
  ).withAutoAcl(true)

}
