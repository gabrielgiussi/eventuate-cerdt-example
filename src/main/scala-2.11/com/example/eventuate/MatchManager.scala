package com.example.eventuate

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.example.eventuate.MatchActor.MatchCommand
import com.rbmhtechnology.eventuate.crdt.CERMatch.MatchService

import scala.concurrent.duration._

class MatchManager(replicaId: String,orSetService: MatchService, eventLog: ActorRef) extends Actor  {

  private implicit val timeout = Timeout(20.seconds)
  private var matchActors: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {
    case m:MatchCommand => matchActor(m.matchId) forward m
  }

  private def matchActor(matchId: String): ActorRef = {
    matchActors.get(matchId) match {
    case Some(orderActor) => orderActor
    case None =>
      matchActors = matchActors + (matchId -> context.actorOf(Props(new MatchActor(matchId, replicaId,orSetService, eventLog))))
      matchActors(matchId)
  }}
}
