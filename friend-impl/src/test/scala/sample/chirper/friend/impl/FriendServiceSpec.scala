package sample.chirper.friend.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import sample.chirper.friend.api
import sample.chirper.friend.api.{FriendId, FriendService, User}

class FriendServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new FriendApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[FriendService]

  override protected def afterAll() = server.stop()

  "Friend service" should {

    "be able to create users and connect friends" in {
      val usr1 = api.CreateUser("usr1", "User 1")
      val usr2 = api.CreateUser("usr2", "User 2")
      val usr3 = api.CreateUser("usr3", "User 3")
      for {
        _ <- client.createUser().invoke(usr1)
        _ <- client.createUser().invoke(usr2)
        _ <- client.createUser().invoke(usr3)
        _ <- client.addFriend("usr1").invoke(FriendId("usr2"))
        _ <- client.addFriend("usr1").invoke(FriendId("usr3"))
        fetchedUsr1 <- client.getUser("usr1").invoke()
      } yield {
        fetchedUsr1 should matchPattern { case User(usr1.userId, usr1.name, _) => }
        fetchedUsr1.friends should ===(List("usr2", "usr3"))
      }
    }

  }

}
