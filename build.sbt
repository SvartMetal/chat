organization := "ru.spbau"

name := "chat"

version := "0.1"

scalaVersion := Version.scala

resolvers += "spray-releases" at "http://repo.spray.io"

libraryDependencies ++= Dependencies.akkaChat

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

initialCommands in console := "import ru.spbau.network.montsev.chat._"
