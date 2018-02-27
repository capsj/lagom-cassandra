package sample.chirper.friend.api

import java.time.Instant

import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

case class User(userId: String, name: String, friends: Seq[String] = Nil)

object User {
  implicit val format = Json.format[User]

  def apply(createUser: CreateUser): User = User(createUser.userId, createUser.name)

}

case class CreateUser(userId: String, name: String)

object CreateUser {
  implicit val format = Json.format[CreateUser]
}

case class FriendAdded(userId: String, friendId: String, timestamp: Instant = Instant.now())

object FriendAdded {
  implicit val format = Json.format[FriendAdded]
}