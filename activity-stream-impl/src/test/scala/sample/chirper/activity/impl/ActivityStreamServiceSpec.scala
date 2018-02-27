package sample.chirper.activity.impl

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import sample.chirper.activity.api.ActivityStreamService
import sample.chirper.chirp.api.{Chirp, ChirpService}
import sample.chirper.friend.api.{FriendService, User}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ActivityStreamServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup) { ctx =>
    new ActivityStreamApplication(ctx) with LocalServiceLocator {
      override lazy val friendService = new FriendServiceStub
      override lazy val chirpService: ChirpService = new ChirpServiceStub
    }
  }

  val client = server.serviceClient.implement[ActivityStreamService]

  override protected def afterAll(): Unit = server.stop()

  "Activity stream service" should {

    "get live feed" in {
      val chirps = Await.result(client.getLiveActivityStream("usr1").invoke(), 3 seconds)
      val probe = chirps.runWith(TestSink.probe(server.actorSystem))(server.materializer)
      probe.request(10)
      probe.expectNext().message should ===("msg1")
      probe.expectNext().message should ===("msg2")
      probe.cancel()
      "1" should ===("1")
    }

    "get historical feed" in {
      val chirps = Await.result(client.getHistoricalActivityStream("usr1").invoke(), 3 seconds)
      val probe = chirps.runWith(TestSink.probe(server.actorSystem))(server.materializer)
      probe.request(10)
      probe.expectNext().message should ===("msg1")
      probe.expectComplete()
      "1" should ===("1")
    }

  }

}

class FriendServiceStub extends FriendService {

  val user1 = User("usr1", "User 1", List("usr2"))
  val user2 = User("usr2", "User 2")

  override def health() = ???

  override def getUser(userId: String) = ServiceCall { _ =>
    userId match {
      case user1.userId => Future.successful(user1)
      case user2.userId => Future.successful(user2)
      case _ => throw NotFound(userId)
    }
  }

  override def createUser() = ???

  override def addFriend(userId: String) = ???

  override def getFollowers(userId: String) = ???

}

class ChirpServiceStub extends ChirpService {

  override def health() = ???

  override def addChirp(userId: String) = ???

  override def getLiveChirps() = ServiceCall { request =>
    if (request.userIds.contains("usr2")) {
      val c1 = Chirp("usr2", "msg1")
      val c2 = Chirp("usr2", "msg2")
      Future.successful(Source(List(c1, c2)))
    } else {
      Future.successful(Source.empty)
    }
  }

  override def getHistoricalChirps() = ServiceCall { request =>
    if (request.userIds.contains("usr2")) {
      val c1 = Chirp("usr2", "msg1")
      Future.successful(Source.single(c1))
    } else {
      Future.successful(Source.empty)
    }
  }

}