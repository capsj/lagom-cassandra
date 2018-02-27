import com.lightbend.lagom.scaladsl.api.{ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.softwaremill.macwire._
import play.api.ApplicationLoader._
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Mode}
import router.Routes

import scala.concurrent.ExecutionContext

class FrontEndLoader extends ApplicationLoader {
  override def load(context: Context): Application = context.environment.mode match {
    case Mode.Dev => (new FrontEndModule(context) with LagomDevModeComponents).application
    case _ => (new FrontEndModule(context) with DnsServiceLocatorComponents).application
  }
}

abstract class FrontEndModule(context: Context)
  extends BuiltInComponentsFromContext(context)
    with I18nComponents
    with AhcWSComponents
    with LagomServiceClientComponents {

  override lazy val serviceInfo = ServiceInfo(
    "front-end",
    Map("front-end" -> List(ServiceAcl.forPathRegex("(?!/api/).*")))
  )

  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  override lazy val router = {
    wire[Routes]
  }

}