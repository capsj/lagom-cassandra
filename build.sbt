import com.typesafe.sbt.packager.docker._
import sbt.Resolver.bintrayRepo

organization in ThisBuild := "com.lightbend.lagom.sample.chirper"
scalaVersion in ThisBuild := "2.11.8"

lazy val buildVersion = sys.props.getOrElse("buildVersion", "1.0.0-SNAPSHOT")

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val serviceLocatorDns = "com.lightbend" % "lagom13-scala-service-locator-dns_2.11" % "2.2.2"

lazy val `lagom-scala-chirper` = (project in file(".")).aggregate(
  `friend-api`, `friend-impl`,
  `chirp-api`, `chirp-impl`,
  `activity-stream-api`, `activity-stream-impl`,
  `front-end`
)

lazy val `friend-api` = (project in file("friend-api"))
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `friend-impl` = (project in file("friend-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $FRIENDSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $FRIENDSERVICE_BIND_PORT")" -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_HOST")" -Dakka.remote.netty.tcp.bind-hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$(eval "echo $AKKA_REMOTING_PORT")" -Dakka.remote.netty.tcp.bind-port="$(eval "echo $AKKA_REMOTING_BIND_PORT")" $(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://friendservice@$NODE"; I=$(expr $I + 1); done)""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c@Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      serviceLocatorDns,
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`friend-api`)

lazy val `chirp-api` = (project in file("chirp-api"))
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `chirp-impl` = (project in file("chirp-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $CHIRPSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $CHIRPSERVICE_BIND_PORT")" -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_HOST")" -Dakka.remote.netty.tcp.bind-hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$(eval "echo $AKKA_REMOTING_PORT")" -Dakka.remote.netty.tcp.bind-port="$(eval "echo $AKKA_REMOTING_BIND_PORT")" $(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://chirpservice@$NODE"; I=$(expr $I + 1); done)""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c@Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      serviceLocatorDns,
      lagomScaladslPersistenceCassandra,
      lagomScaladslPubSub,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`chirp-api`)

lazy val `activity-stream-api` = (project in file("activity-stream-api"))
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`chirp-api`)

lazy val `activity-stream-impl` = (project in file("activity-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $ACTIVITYSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $ACTIVITYSERVICE_BIND_PORT")"""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c@Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      serviceLocatorDns,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(BuildTarget.additionalSettings)
  //  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`activity-stream-api`, `chirp-api`, `friend-api`)

lazy val `front-end` = (project in file("front-end"))
  .enablePlugins(PlayScala, LagomPlay)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    routesGenerator := InjectedRoutesGenerator,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $WEB_BIND_IP")" -Dhttp.port="$(eval "echo $WEB_BIND_PORT")"""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c@Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    routesGenerator := StaticRoutesGenerator,
    libraryDependencies ++= Seq(
      "org.webjars" % "react" % "0.14.3",
      "org.webjars" % "react-router" % "1.0.3",
      "org.webjars" % "jquery" % "2.2.0",
      "org.webjars" % "foundation" % "5.3.0",
      serviceLocatorDns,
      macwire,
      lagomScaladslServer
      // TODO needed ?, lagomScaladslClient
    ),
    ReactJsKeys.sourceMapInline := true
  )
  .settings(BuildTarget.additionalSettings)

//// do not delete database files on start
//lagomCassandraCleanOnStart in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false



