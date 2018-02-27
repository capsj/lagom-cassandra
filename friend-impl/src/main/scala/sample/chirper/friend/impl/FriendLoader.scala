package sample.chirper.friend.impl

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import sample.chirper.friend.api.FriendService

class FriendLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) = new FriendApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) = new FriendApplication(context) with DnsServiceLocatorComponents

  override def describeService = Some(readDescriptor[FriendService])

}

abstract class FriendApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[FriendService](wire[FriendServiceImpl])

  override def jsonSerializerRegistry = FriendSerializerRegistry

  persistentEntityRegistry.register(wire[FriendEntity])

  readSide.register(wire[FriendEventProcessor])

}

