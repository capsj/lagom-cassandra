package sample.chirper.friend.api

import play.api.libs.json.{Json, OFormat}

case class SOSMessage(id: String, date: String, time: String, position: String, speed: String, direction: String, eventCode: String, altitude: String,
                      battery: String, satellite: String, satelliteAmount: String)

object SOSMessage {
  implicit val format: OFormat[SOSMessage] = Json.format

  def apply(rawMessage: String): SOSMessage = {
    val values = rawMessage.split(",")
    SOSMessage("testID", values(1), values(2), values(3), values(4), values(5), values(6), values(7), values(8), values(9), values(10))
  }
}
