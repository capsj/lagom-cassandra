package sample.chirper.activity.impl

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import sample.chirper.activity.api.ActivityStreamService
import sample.chirper.chirp.api.ChirpService
import sample.chirper.friend.api.FriendService

class ActivityStreamLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) = new ActivityStreamApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) = new ActivityStreamApplication(context) with DnsServiceLocatorComponents

  override def describeService = Some(readDescriptor[ActivityStreamService])

}

abstract class ActivityStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[ActivityStreamService](wire[ActivityStreamServiceImpl])

  lazy val friendService = serviceClient.implement[FriendService]
  lazy val chirpService = serviceClient.implement[ChirpService]

}
