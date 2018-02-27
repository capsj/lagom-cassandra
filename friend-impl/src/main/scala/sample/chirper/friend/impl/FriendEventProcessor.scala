package sample.chirper.friend.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import sample.chirper.friend.api.SOSMessage

import scala.concurrent.{ExecutionContext, Future}

class FriendEventProcessor(
                            session: CassandraSession,
                            readSide: CassandraReadSide
                          )(implicit ec: ExecutionContext) extends ReadSideProcessor[FriendEvent] {

  private var writeFollowers: PreparedStatement = _
  private var writeMessages: PreparedStatement = _

  override def buildHandler() = readSide.builder[FriendEvent]("friend_offset")
    .setGlobalPrepare(prepareCreateTables)
    .setPrepare(_ => prepareWriteFollowers())
    .setEventHandler[FriendAdded](ese => processFriendChanged(ese.event))
    .setPrepare(_ => prepareWriteMessages)
    .setEventHandler[MessageAdded](x => processMessageChanged(x.event))
    .build()

  override def aggregateTags = Set(FriendEvent.Tag)

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def prepareCreateTables(): Future[Done] = {
    session.executeCreateTable(
      """CREATE TABLE IF NOT EXISTS follower (
        |userId text, followedBy text,
        |PRIMARY KEY (userId, followedBy)
        |)""".stripMargin) flatMap (_ =>
      session.executeCreateTable(
        """CREATE TABLE IF NOT EXISTS message (
          |id text, date text, time text, position text, speed text, direction text, eventCode text, altitude text,
          |battery text, satellite text, satelliteAmount text,
          |PRIMARY KEY (id)
          |)""".stripMargin)
      )
  }

  private def prepareWriteFollowers(): Future[Done] = {
    session.prepare("INSERT INTO follower (userId, followedBy) VALUES (?, ?)").map { ps =>
      writeFollowers = ps
      Done
    }
  }

//  private def writeMessage(message: SOSMessage): Future[Done] = {
//    session.prepare("INSERT INTO message (id, date, time, position, speed, direction, eventCode, altitude, battery, satellite, satelliteAmount) " +
//      "VALUES (" + message.id + ", " + message.date + ", " + message.time + ", " + message.position + ", " + message.speed + ", " + message.direction + "," +
//      ", " + message.eventCode + ", " + message.altitude + ", " + message.battery + ", " + message.satellite + ", " + message.satelliteAmount + ")") map { ps =>
//      writeMessages = ps
//      Done
//    }
//  }

  private def prepareWriteMessages: Future[Done] = {
    session.prepare("INSERT INTO message (id, date, time, position, speed, direction, eventCode, altitude, battery, satellite, satelliteAmount) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") map { ps =>
      writeMessages = ps
      Done
    }
  }

  private def processFriendChanged(event: FriendAdded): Future[List[BoundStatement]] = {
    val bindWriteFollowers = writeFollowers.bind
    bindWriteFollowers.setString("userId", event.friendId)
    bindWriteFollowers.setString("followedBy", event.userId)
    Future.successful(List(bindWriteFollowers))
  }

  private def processMessageChanged(event: MessageAdded): Future[List[BoundStatement]] = {
    val bindWriteMessages = writeMessages.bind
    bindWriteMessages.setString("id", event.message.id)
    bindWriteMessages.setString("date", event.message.date)
    bindWriteMessages.setString("time", event.message.time)
    bindWriteMessages.setString("position", event.message.position)
    bindWriteMessages.setString("speed", event.message.speed)
    bindWriteMessages.setString("direction", event.message.direction)
    bindWriteMessages.setString("eventCode", event.message.eventCode)
    bindWriteMessages.setString("altitude", event.message.altitude)
    bindWriteMessages.setString("battery", event.message.battery)
    bindWriteMessages.setString("satellite", event.message.satellite)
    bindWriteMessages.setString("satelliteAmount", event.message.satelliteAmount)
//    writeMessage(event.message) map { _ =>
    Future.successful(List(bindWriteMessages))
//    }
  }
}
