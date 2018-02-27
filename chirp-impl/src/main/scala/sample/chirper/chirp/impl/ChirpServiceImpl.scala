package sample.chirper.chirp.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import sample.chirper.chirp.api.{Chirp, ChirpService}

import scala.concurrent.{ExecutionContext, Future}

class ChirpServiceImpl(
                        persistentEntities: PersistentEntityRegistry,
                        chirpTopic: ChirpTopic,
                        chirps: ChirpRepository
                      )(implicit ec: ExecutionContext) extends ChirpService {

  override def health() = ServiceCall { _ => Future.successful("OK") }

  override def addChirp(userId: String) = ServiceCall { request =>
    if (userId != request.userId) throw new IllegalArgumentException(s"UserId $userId did not match userId in $request")
    persistentEntities.refFor[ChirpTimelineEntity](userId)
      .ask(AddChirp(Chirp(request)))
      .map(_ => NotUsed.getInstance())
  }

  override def getLiveChirps() = ServiceCall { request =>
    chirps.getRecentChirps(request.userIds).map { recentChirps =>
      val sources = request.userIds.map(chirpTopic.subscriber)
      val users = request.userIds.distinct
      val publishedChirps = Source(sources)
        .flatMapMerge(sources.size, identity)
        .filter(c => users.contains(c.userId))

      // We currently ignore the fact that it is possible to get duplicate chirps
      // from the recent and the topic. That can be solved with a de-duplication stage.
      Source(recentChirps).concat(publishedChirps)
    }
  }

  override def getHistoricalChirps() = ServiceCall { request =>
    val timestamp = request.fromTime.toEpochMilli
    val result = chirps.getHistoricalChirps(request.userIds, timestamp)
    Future.successful(result)
  }

}
