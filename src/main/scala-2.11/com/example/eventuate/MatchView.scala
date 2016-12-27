package com.example.eventuate

import akka.actor.ActorRef
import com.example.eventuate.MatchActor.{MatchCreated}
import com.rbmhtechnology.eventuate.{Versioned, EventsourcedView}
import com.rbmhtechnology.eventuate.crdt.{Apology, AddOp}
import com.rbmhtechnology.eventuate.crdt.CRDTService.ValueUpdated

object MatchView{

  case class GetMatchs()
  case class GetMatch(matchId: String)

  case class GetMatchsSuccess(matchs: Set[Match])
  case class GetMatchSuccess(matchId: String, m: Match)
  case class GetMatchFailure(matchId: String)
}

class MatchView(val id: String, val eventLog: ActorRef) extends EventsourcedView {
  import MatchView._

  var matchs: Map[String,Match] = Map.empty

  override def onEvent: Receive = {
    case MatchCreated(matchId,creator,date,place) => {
      println("Se recibio el match con id: " + matchId)
      matchs = matchs + (matchId -> Match(matchId,creator,Set.empty,date,place))
    }
    case ValueUpdated(op) => op match {
      case AddOp(player) => {
        val matchId = lastEmitterAggregateId.get.replaceFirst("CERMatch_match-","")
        matchs get (matchId) match {
          case Some(m) => {
        val updatedMatch = m.copy(players = m.players + player.toString)
        matchs = matchs + (matchId -> updatedMatch)
        }
          case None => println("No encontro match con id: " +lastEmitterAggregateId + " Los matchs son" + matchs)
        }

      }
      case e => println("MatchView event discarded. "+ e.toString)
    }
    case Apology(Versioned(e1,_,_,_),Versioned(e2,_,_,_)) => {
      val matchId = lastEmitterAggregateId.get.replaceFirst("CERMatch_match-", "")
      println("Id de apology: " + matchId)
      matchs get (matchId) match {
        case Some(m) => {
          val updatedMatch = m.copy(players = m.players - e1.toString)
          matchs = matchs + (matchId -> updatedMatch)
        }
      }
    }
  }

  override def onCommand: Receive = {
    case GetMatchs() => sender() ! GetMatchsSuccess(matchs.values.to[Set])
    case GetMatch(id) => matchs get id match {
      case Some(m) => sender() ! GetMatchSuccess(id,m)
      case None => sender() ! GetMatchFailure(id)
    }
  }

}
