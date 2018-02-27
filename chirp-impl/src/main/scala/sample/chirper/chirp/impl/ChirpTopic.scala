package sample.chirper.chirp.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRef, PubSubRegistry, TopicId}
import sample.chirper.chirp.api.Chirp

trait ChirpTopic {

  def publish(chirp: Chirp): Unit

  def subscriber(userId: String): Source[Chirp, NotUsed]

}

object ChirpTopic {
  val MaxTopics = 1024
}

class ChirpTopicImpl(pubSubRegistry: PubSubRegistry) extends ChirpTopic {

  override def publish(chirp: Chirp): Unit = {
    refFor(chirp.userId).publish(chirp)
  }

  override def subscriber(userId: String): Source[Chirp, NotUsed] = {
    refFor(userId).subscriber
  }

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def refFor(userId: String): PubSubRef[Chirp] = {
    val tmp: TopicId[Chirp] = TopicId(topicQualifier(userId))
    pubSubRegistry.refFor(tmp)
  }

  private def topicQualifier(userId: String): String = {
    String.valueOf(Math.abs(userId.hashCode) % ChirpTopic.MaxTopics)
  }

}
