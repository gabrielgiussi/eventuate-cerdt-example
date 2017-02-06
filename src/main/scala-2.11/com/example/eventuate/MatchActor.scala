package com.example.eventuate

import java.util.Date

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedActor
import com.rbmhtechnology.eventuate.crdt.MatchService

import scala.util.{Failure, Success}

object MatchActor {

  trait MatchCommand {
    def matchId: String
  }

  trait MatchEvent {
    def matchId: String
  }

  // No es raro que un Actor ya creado reciba un CreateMatch? Que pasa si el CreateMatch falla?. Queda un actor en el aire?
  // https://github.com/RBMHTechnology/eventuate/blob/master/eventuate-examples/src/main/scala/com/rbmhtechnology/example/ordermgnt/OrderActor.scala
  case class CreateMatch(matchId: String, creator: String, date: Date, place: String) extends MatchCommand

  case class AddPlayer(matchId: String, player: String) extends MatchCommand

  case class RemovePlayer(matchId: String, player: String) extends MatchCommand

  case class MatchCreated(matchId: String,creator: String, date: Date, place: String) extends MatchEvent

  case class CreateMatchSuccess(matchId: String, m: Match)

  case class CommandSuccess(matchId: String)
  case class CommandFailure(matchId: String, cause: Throwable)

}
class MatchActor(matchId: String, replicaId: String, orSetService: MatchService,val eventLog: ActorRef) extends EventsourcedActor {

  import MatchActor._
  import akka.pattern.pipe
  import context.dispatcher

  private var date: Date = null
  private var place: String = ""


  override val id = s"s-${matchId}-${replicaId}"
  val matchCRDTId = s"match-${matchId}"
  override val aggregateId = Some(matchId)

  override def onCommand: Receive = {
    case create: CreateMatch => persist(MatchCreated(create.matchId,create.creator,create.date,create.place)) {
      case Success(m) => sender() ! CreateMatchSuccess(create.matchId, Match(create.matchId,create.creator,Set.empty,create.date,create.place))
      case Failure(t) => sender() ! CommandFailure(create.matchId,t)
    }
    case add: AddPlayer => pipe(orSetService.add(matchCRDTId,add.player) map { _ => CommandSuccess(matchId) } recover { case x => CommandFailure(matchId,x)}) to sender() // Lo puedo hacer directamente asi o guardo un evento y en el avento hago el add?
    case remove: RemovePlayer => orSetService.remove(matchCRDTId,remove.player)
    //case get:GetMatch => orSetService.value(matchCRDTId) map (players => GetMatchSuccess(Match(matchId, players, date, place))) pipeTo sender()

  }

  override def onEvent: Receive = {
    case e:MatchCreated => {
      date = e.date
      place = e.place
      //orSetService.add(matchCRDTId,e.creator) // Que pasa si falla antes de invocar el servicio? Que pasa en recovering?
    }
  }



}
