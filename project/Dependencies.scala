import sbt._

object Version {
  val scala     = "2.10.3"
  val akka      = "2.3.0"
  val spray     = "1.3.0"
  val sprayJson = "1.2.5"
  val logback   = "1.0.13"
  val scalaTest = "2.0"
}

object Library {
  val akkaActor      = "com.typesafe.akka" %% "akka-actor"      % Version.akka
  val akkaSlf4j      = "com.typesafe.akka" %% "akka-slf4j"      % Version.akka
  val akkaCluster    = "com.typesafe.akka" %% "akka-cluster"    % Version.akka
  val akkaTestkit    = "com.typesafe.akka" %% "akka-testkit"    % Version.akka
  val akkaSTM        = "com.typesafe.akka" %% "akka-stm"        % Version.akka
  val sprayRouting   = "io.spray"          %  "spray-routing"   % Version.spray
  val sprayCan       = "io.spray"          %  "spray-can"       % Version.spray
  val sprayJson      = "io.spray"          %% "spray-json"      % Version.sprayJson
  val logbackClassic = "ch.qos.logback"    %  "logback-classic" % Version.logback
  val scalaTest      = "org.scalatest"     %% "scalatest"       % Version.scalaTest
}

object Dependencies {

  import Library._

  val akkaChat = List(
    akkaActor,
    akkaSlf4j,
    akkaCluster,
    sprayRouting,
    sprayCan,
    sprayJson,
    logbackClassic,
    scalaTest   % "test",
    akkaTestkit % "test"
  )
}
