name := "eventuate-cerdt"

version := "7.0"

scalaVersion := "2.11.8"

mainClass in Compile := Some("com.example.eventuate.EventuateMatchs")

enablePlugins(JavaAppPackaging)

dockerExposedPorts := Seq(8080,8081,2552,2553)

//val ivyLocal = Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
resolvers += "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local"
//resolvers += ivyLocal

val eventuateOrg = "oss.gabrielgiussi"
val eventuateVersion = "0.9-SNAPSHOT"
val akkaVersion = "2.4.16"
val akkaHttpVersion = "10.0.0"
val Log4jVersion = "2.5"
val reactiveMongoVersion = "0.12.0"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value,
  eventuateOrg %% "eventuate-core" % eventuateVersion,
  eventuateOrg %% "eventuate-log-leveldb" % eventuateVersion,
  eventuateOrg %% "eventuate-crdt" % eventuateVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % Log4jVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion
)
