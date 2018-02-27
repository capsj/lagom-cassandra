package sample.chirper.chirp.impl

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import sample.chirper.chirp.api.Chirp

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

/**
  * Provides access to past chirps. See {@link ChirpTopic} for real-time access to new chirps.
  */
trait ChirpRepository {

  def getHistoricalChirps(userIds: Seq[String], timestamp: Long): Source[Chirp, NotUsed]

  def getRecentChirps(userIds: Seq[String]): Future[Seq[Chirp]]

}

class ChirpRepositoryImpl(
                           db: CassandraSession
                         )(implicit val ec: ExecutionContext) extends ChirpRepository {

  private val NumRecentChirps = 10
  private val SelectHistoricalChirps = "SELECT * FROM chirp WHERE userId = ? AND timestamp >= ? ORDER BY timestamp ASC"
  private val SelectRecentChirps = "SELECT * FROM chirp WHERE userId = ? ORDER BY timestamp DESC LIMIT ?"

  override def getHistoricalChirps(userIds: Seq[String], timestamp: Long): Source[Chirp, NotUsed] = {
    val sources = userIds.map(getHistoricalChirps(_, timestamp))
    // Chirps from one user are ordered by timestamp, but chirps from different
    // users are not ordered. That can be improved by implementing a smarter
    // merge that takes the timestamps into account.
    Source(sources).flatMapMerge(sources.size, identity)
  }

  override def getRecentChirps(userIds: Seq[String]): Future[Seq[Chirp]] =
    Future
    .sequence(userIds.map(getRecentChirps))
    .map(_.flatten)
    .map(limitRecentChirps)

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def getHistoricalChirps(userId: String, timestamp: Long): Source[Chirp, NotUsed] =
    db.select(
      SelectHistoricalChirps,
      userId,
      Long.box(timestamp)
    ).map(mapChirp)

  private def getRecentChirps(userId: String) =
    db.selectAll(
      SelectRecentChirps,
      userId,
      Int.box(NumRecentChirps)
    ).map(_.map(mapChirp))

  private def mapChirp(row: Row): Chirp = Chirp(
    row.getString("userId"),
    row.getString("message"),
    Instant.ofEpochMilli(row.getLong("timestamp")),
    row.getString("uuid")
  )

  private def limitRecentChirps(all: Seq[Chirp]): Seq[Chirp] = {
    // FIXME: this can be streamed
    val limited = all
      .sortWith(_.timestamp.toEpochMilli < _.timestamp.toEpochMilli)
      .take(NumRecentChirps)
    limited.reverse
  }

}