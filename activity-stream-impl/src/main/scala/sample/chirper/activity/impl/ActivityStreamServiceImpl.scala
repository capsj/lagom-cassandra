package sample.chirper.activity.impl

import java.time.{Duration, Instant}

import com.lightbend.lagom.scaladsl.api.ServiceCall
import sample.chirper.activity.api.ActivityStreamService
import sample.chirper.chirp.api.{ChirpService, HistoricalChirpsRequest, LiveChirpRequest}
import sample.chirper.friend.api.FriendService

import scala.concurrent.{ExecutionContext, Future}

class ActivityStreamServiceImpl(
                                 friendService: FriendService,
                                 chirpService: ChirpService
                               )
                               (implicit val ec: ExecutionContext) extends ActivityStreamService {


  override def health() = ServiceCall { _ => Future.successful("OK") }

  override def getLiveActivityStream(userId: String) = ServiceCall { _ =>
    for {
      user <- friendService.getUser(userId).invoke()
      userIds = user.friends :+ userId
      chirpsReq = LiveChirpRequest(userIds)
      // Note that this stream will not include changes to friend associates,
      // e.g. adding a new friend.
      results <- chirpService.getLiveChirps().invoke(chirpsReq)
    } yield results
  }

  override def getHistoricalActivityStream(userId: String) = ServiceCall { _ =>
    for {
      user <- friendService.getUser(userId).invoke()
      userIds = user.friends :+ userId
      // FIXME we should use HistoricalActivityStreamReq request parameter
      fromTime = Instant.now().minus(Duration.ofDays(7))
      chirpsReq = HistoricalChirpsRequest(fromTime, userIds)
      results <- chirpService.getHistoricalChirps().invoke(chirpsReq)
    } yield results
  }

}
