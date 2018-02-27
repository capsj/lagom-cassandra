package sample.chirper.chirp.impl

import java.time.Instant

import akka.stream.testkit.scaladsl.TestSink
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import sample.chirper.chirp.api
import sample.chirper.chirp.api.{ChirpService, HistoricalChirpsRequest, LiveChirpRequest}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

class ChirpServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll with Eventually {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new ChirpApplication(ctx) with LocalServiceLocator
  }

  val chirpService = server.serviceClient.implement[ChirpService]

  override protected def afterAll() = server.stop()

  "Chirp service" should {

    "publish chirps to subscribers" in {
      val request = LiveChirpRequest(List("usr1", "usr2"))

      val chirps1 = Await.result(chirpService.getLiveChirps().invoke(request), 3 seconds)
      val probe1 = chirps1.runWith(TestSink.probe(server.actorSystem))(server.materializer)
      probe1.request(10)

      val chirps2 = Await.result(chirpService.getLiveChirps().invoke(request), 3 seconds)
      val probe2 = chirps2.runWith(TestSink.probe(server.actorSystem))(server.materializer)
      probe2.request(10)

      val addChirp1 = api.AddChirp("usr1", "hello 1")
      Await.result(chirpService.addChirp("usr1").invoke(addChirp1), 3 seconds)
      probe1.expectNext().message should ===(addChirp1.message)
      probe2.expectNext().message should ===(addChirp1.message)

      val addChirp2 = api.AddChirp("usr1", "hello 2")
      Await.result(chirpService.addChirp("usr1").invoke(addChirp2), 3 seconds)
      probe1.expectNext().message should ===(addChirp2.message)
      probe2.expectNext().message should ===(addChirp2.message)

      val addChirp3 = api.AddChirp("usr2", "hello 3")
      Await.result(chirpService.addChirp("usr2").invoke(addChirp3), 3 seconds)
      probe1.expectNext().message should ===(addChirp3.message)
      probe2.expectNext().message should ===(addChirp3.message)

      probe1.cancel()
      probe2.cancel()

      // FIXME
      "1" should ===("1")

    }

    "include some old chirps in live feed" in {

      val addChirp1 = api.AddChirp("usr3", "hi 1")
      Await.result(chirpService.addChirp("usr3").invoke(addChirp1), 3 seconds)

      val addChip2 = api.AddChirp("usr4", "hi 2")
      Await.result(chirpService.addChirp("usr4").invoke(addChip2), 3 seconds)

      val request = LiveChirpRequest(List("usr3", "usr4"))

      eventually(timeout(Span(10, Seconds))) {

        val chirps = Await.result(chirpService.getLiveChirps().invoke(request), 3 seconds)
        val probe = chirps.runWith(TestSink.probe(server.actorSystem))(server.materializer)
        probe.request(10)
        var expectedMsgs = ArrayBuffer("hi 1", "hi 2")
        expectedMsgs -= probe.expectNext().message
        expectedMsgs -= probe.expectNext().message
        expectedMsgs.size should ===(0)

        val addChirp3 = api.AddChirp("usr4", "hi 3")
        Await.result(chirpService.addChirp("usr4").invoke(addChirp3), 3 seconds)
        probe.expectNext().message should ===("hi 3")

        probe.cancel()

      }

      // FIXME
      "1" should ===("1")

    }

    "retrieve old chirps" in {

      val addChirp1 = api.AddChirp("usr5", "msg 1")
      Await.result(chirpService.addChirp("usr5").invoke(addChirp1), 3 seconds)

      val addChirp2 = api.AddChirp("usr6", "msg 2")
      Await.result(chirpService.addChirp("usr6").invoke(addChirp2), 3 seconds)

      val request = new HistoricalChirpsRequest(
        Instant.now().minusSeconds(20),
        List("usr5", "usr6")
      )

      eventually(timeout(Span(10, Seconds))) {
        val chirps = Await.result(chirpService.getHistoricalChirps().invoke(request), 3 seconds)
        val probe = chirps.runWith(TestSink.probe(server.actorSystem))(server.materializer)
        probe.request(10)
        var expectedMsgs = ArrayBuffer("msg 1", "msg 2")
        expectedMsgs -= probe.expectNext().message
        expectedMsgs -= probe.expectNext().message
        expectedMsgs.size should ===(0)
        probe.expectComplete()
      }

      // FIXME
      "1" should ===("1")
    }

  }

}





















