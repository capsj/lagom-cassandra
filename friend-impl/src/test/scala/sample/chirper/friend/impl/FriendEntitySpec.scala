package sample.chirper.friend.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import sample.chirper.friend.api.User

class FriendEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("FriendEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(FriendSerializerRegistry))

  override protected def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  def withTestDriver(test: PersistentEntityTestDriver[FriendCommand[_], FriendEvent, FriendState] => Unit): Unit = {
    val testDriver = new PersistentEntityTestDriver(system, new FriendEntity, "user-1")
    test(testDriver)
    testDriver.getAllIssues should have size 0
  }

  "Friend entity" should {

    "not be initialized by default" in withTestDriver { driver =>
      val outcome = driver.run(GetUser())
      outcome.replies should contain only GetUserReply(None)
    }

    "create user" in withTestDriver { driver =>
      val alice = User("alice", "Alice")
      val outcome = driver.run(CreateUser(alice))
      outcome.replies should contain only Done
      outcome.events.size should ===(1)
      outcome.events.head should matchPattern { case UserCreated("alice", "Alice", _) => }
    }

    "reject duplicate create" in withTestDriver { driver =>
      val alice = User("alice", "Alice")
      driver.run(CreateUser(alice))
      val outcome = driver.run(CreateUser(alice))
      outcome.replies should contain only InvalidCommandException("User Alice is already created")
    }

    "create user with initial friends" in withTestDriver { driver =>
      val alice = User("alice", "Alice", List("bob", "peter"))
      val outcome = driver.run(CreateUser(alice))
      outcome.replies should contain only Done
      outcome.events.size should ===(3)
      outcome.events(0) should matchPattern { case UserCreated("alice", "Alice", _) => }
      outcome.events(1) should matchPattern { case FriendAdded("alice", "bob", _) => }
      outcome.events(2) should matchPattern { case FriendAdded("alice", "peter", _) => }
    }

    "not add friend if not initialized" in withTestDriver { driver =>
      val outcome = driver.run(AddFriend("bob"))
      outcome.replies should contain only InvalidCommandException("User user-1 is not created")
    }

    "add friend" in withTestDriver { driver =>
      val alice = User("alice", "Alice")
      driver.run(CreateUser(alice))
      val outcome = driver.run(AddFriend("bob"), AddFriend("peter"))
      outcome.replies should contain only Done
      outcome.events(0) should matchPattern { case FriendAdded("alice", "bob", _) => }
      outcome.events(1) should matchPattern { case FriendAdded("alice", "peter", _) => }
    }

    "add duplicate friend" in withTestDriver { driver =>
      val alice = User("alice", "Alice")
      driver.run(CreateUser(alice))
      driver.run(AddFriend("bob"), AddFriend("peter"))
      val outcome = driver.run(AddFriend("bob"))
      outcome.replies should contain only Done
      outcome.events.size should ===(0)
    }

    "get user" in withTestDriver {driver =>
      val alice = User("alice", "Alice")
      driver.run(CreateUser(alice))
      val outcome = driver.run(GetUser())
      outcome.replies should contain only GetUserReply(Some(alice))
      outcome.events.size should ===(0)
    }

  }

}
