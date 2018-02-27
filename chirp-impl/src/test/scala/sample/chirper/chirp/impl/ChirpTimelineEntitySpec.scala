package sample.chirper.chirp.impl

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import sample.chirper.chirp.api.Chirp

class ChirpTimelineEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("ChirpTimelineEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(ChirpTimelineSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def withTestDriver(test: PersistentEntityTestDriver[ChirpTimelineCommand[_], ChirpTimelineEvent, NotUsed] => Unit): Unit = {
    val topic = new ChirpTopicStub
    val testDriver = new PersistentEntityTestDriver(system, new ChirpTimelineEntity(topic), "chirp-1")
    test(testDriver)
    testDriver.getAllIssues should have size 0
  }

  "Chirp timeline entity" should {

    "add chirp" in withTestDriver { driver =>
      val chirp = Chirp("user-1", "Hello, world")
      val outcome = driver.run(new AddChirp(chirp))
      outcome.replies should contain only Done
      outcome.events.size should ===(1)
      outcome.events(0).asInstanceOf[ChirpAdded].chirp should ===(chirp)
    }

  }

}

class ChirpTopicStub extends ChirpTopic {

  var chirps = List.empty[Chirp]

  override def publish(chirp: Chirp): Unit = chirps = chirps :+ chirp

  override def subscriber(userId: String): Source[Chirp, NotUsed] = Source(chirps)

}