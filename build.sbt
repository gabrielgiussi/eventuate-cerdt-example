name := "eventuate-cerdt"

version := "1.0"

scalaVersion := "2.11.8"

version in ThisBuild := "0.1"

val ivyLocal = Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
resolvers += "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local" + ivyLocal

//val eventuateVersion = "0.8-SNAPSHOT"
//val eventuateVersion = "0.8-CRDTOP"
val eventuateVersion = "0.8.2-CERDT"
val akkaVersion = "2.4.4"
val Log4jVersion = "2.5"
val reactiveMongoVersion = "0.12.0"

libraryDependencies ++= Seq(
  "com.rbmhtechnology" %% "eventuate-core" % eventuateVersion,
  "com.rbmhtechnology" %% "eventuate-log-leveldb" % eventuateVersion,
  "com.rbmhtechnology" %% "eventuate-crdt" % eventuateVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % Log4jVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion
)