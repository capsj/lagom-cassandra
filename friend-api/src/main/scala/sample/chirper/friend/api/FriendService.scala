package sample.chirper.friend.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection.immutable.Seq

trait FriendService extends Service {

  def health(): ServiceCall[NotUsed, String]

  def getUser(userId: String): ServiceCall[NotUsed, User]

  def createUser(): ServiceCall[CreateUser, NotUsed]

  def addFriend(userId: String): ServiceCall[FriendId, NotUsed]

  def getFollowers(userId: String): ServiceCall[NotUsed, Seq[String]]

  def saveMessage(rawMessage: String): ServiceCall[NotUsed, SOSMessage]

  override def descriptor = named("friendservice").withCalls(
    pathCall("/api/users/health", health _),
    pathCall("/api/users/:userId", getUser _),
    namedCall("/api/users", createUser _),
    pathCall("/api/users/:userId/friends", addFriend _),
    pathCall("/api/users/:userId/followers", getFollowers _),
    pathCall("/api/message/:rawMessage", saveMessage _)
  ).withAutoAcl(true)
}

