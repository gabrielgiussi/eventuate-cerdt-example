package com.example.eventuate

import akka.actor.{ActorRef, Props}
import akka.stream.actor.ActorPublisher
import com.rbmhtechnology.eventuate.crdt.Apology
import com.rbmhtechnology.eventuate.{EventsourcedActor, Versioned}

object EducatedActor {
  def props(id: String, eventLog: ActorRef): Props = Props(new EducatedActor(id,eventLog))

  case class MatchApology(matchId: String, removed: String, player: String)

  case class AcceptApology(matchId: String, player: String)

  case class ApologyAccepted(matchId: String, player:String)
}

class EducatedActor(val id: String, val eventLog: ActorRef) extends EventsourcedActor with ActorPublisher[EducatedActor.MatchApology] {

  import EducatedActor._

  override def onCommand: Receive = {
    case AcceptApology(matchId,player) => persist(ApologyAccepted(matchId,player)){
      case e => sender() ! e
    }
  }

  override def onEvent: Receive = {
    case Apology(Versioned(e1,_,_,_),Versioned(e2,_,_,_)) if (!recovering) => {
    //case Apology(Versioned(e1,_,_,_),Versioned(e2,_,_,_)) => {
      val matchId = lastEmitterAggregateId.get.replaceFirst("CERMatch_match-","")
      onNext(MatchApology(matchId,e1.asInstanceOf[String], e2.asInstanceOf[String]))
    }

  }
}
