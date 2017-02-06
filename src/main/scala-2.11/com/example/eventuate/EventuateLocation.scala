package com.example.eventuate

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.rbmhtechnology.eventuate.ReplicationConnection._
import com.rbmhtechnology.eventuate.ReplicationEndpoint
import com.rbmhtechnology.eventuate.crdt.{CERDTAwaker, CounterService, MatchService}
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory

class EventuateLocation(val locationId: String, port: Int, val mongoUri: String, val dbname: String) {

  implicit val system = ActorSystem(DefaultRemoteSystemName, ConfigFactory.load(locationId))
  val endpoint = ReplicationEndpoint(id => LeveldbEventLog.props(id))(system)
  endpoint.activate()
  val matchService = new MatchService("matchService",endpoint.logs(ReplicationEndpoint.DefaultLogName))
  val manager = system.actorOf(Props(new MatchManager(endpoint.id,matchService, endpoint.logs(ReplicationEndpoint.DefaultLogName))))
  val view = system.actorOf(Props(new MatchView(endpoint.id, endpoint.logs(ReplicationEndpoint.DefaultLogName))))
  val educatedActorSource: Source[EducatedActor.MatchApology,ActorRef] = Source.actorPublisher[EducatedActor.MatchApology](EducatedActor.props(endpoint.id,endpoint.logs(ReplicationEndpoint.DefaultLogName)))
  // TODO emprolijar esto. Use el mismo actor pero tuve q crear una instancia que funciona como source y otra para mandarle los comandos
  // podria hacer que el actor sea Publisher y Suscriber al mismo tiempo?
  val educatedActor = system.actorOf(EducatedActor.props(endpoint.id,endpoint.logs(ReplicationEndpoint.DefaultLogName)))

  // Que pasa si la conexion se cae? Se vuelve a levantar sola?
  val mongoCollections = new MongoCollections(mongoUri,dbname,system.dispatcher)

  val apologiesWriter = system.actorOf(Props(new ApologiesWriter(endpoint.id,endpoint.logs(ReplicationEndpoint.DefaultLogName),mongoCollections)))

  val awaker = system.actorOf(Props(new CERDTAwaker(endpoint.id,matchService,endpoint.logs(ReplicationEndpoint.DefaultLogName))))

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

 val server = new MatchServer(port,system,materializer,executionContext,manager,view,educatedActorSource,educatedActor,mongoCollections)



}






