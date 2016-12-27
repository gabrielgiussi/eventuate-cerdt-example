package com.example.eventuate

import akka.actor.Actor.emptyBehavior
import akka.actor.ActorRef
import com.example.eventuate.EducatedActor.ApologyAccepted
import com.rbmhtechnology.eventuate.EventsourcedWriter
import com.rbmhtechnology.eventuate.crdt.Apology
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.DefaultBSONHandlers

import scala.concurrent.Future


class ApologiesWriter(val id:String, val eventLog: ActorRef, mongoCollections: MongoCollections) extends EventsourcedWriter[Long,Unit] with DefaultBSONHandlers {

  import context.dispatcher
  import reactivemongo.bson._

  type DelayedOp = () => Future[WriteResult]

  /*def db1: Future[DefaultDB] = connection.flatMap(_.database(dbname))
  def apologies: Future[BSONCollection] = db1.map(_.collection("apologies"))
  def progress: Future[BSONCollection] = db1.map(_.collection("progress"))
  */
  def delayedUpdate(selector: BSONDocument, doc: BSONDocument,upsert: Boolean): DelayedOp = () => mongoCollections.apologies.flatMap(_.update(selector,doc,upsert = upsert))

  //var batch: Vector[BSONDocument] = Vector.empty
  var batch: Vector[DelayedOp] = Vector.empty

  val progressSelector = BSONDocument("_id" -> 0)

  // TODO esto significa que envia a la base batchs de x elementos?
  // Si yo le pongo batchSize 16 no manda a la base hasta no tener 16
  override def replayBatchSize: Int = 1

  // TODO QUE HACER SI ESTA QUERY FALLA?
  override def read(): Future[Long] = mongoCollections.progress.flatMap { _.find(progressSelector).requireOne[BSONDocument] map { _.getAs[BSONNumberLike]("sequence_nr").get.toLong }}

  override def readSuccess(result: Long): Option[Long] = Some(result + 1L)

  override def write(): Future[Unit] = {
    val snr = lastSequenceNr
    val res = for {
      _ <- Future.sequence(batch.map(insert => insert()))
      _ <- mongoCollections.progress.flatMap(_.update(progressSelector,BSONDocument("sequence_nr" -> snr)))
    } yield ()
    batch = Vector.empty // clear batch
    res onFailure {case e => e.printStackTrace() }
    res
  }

  override def onEvent: Receive = {
    case a:Apology => {
      val matchId = lastEmitterAggregateId.get.replaceFirst("CERMatch_match-","")
      val id: String = matchId + "_" + a.undo.value
      val doc = BSONDocument("$setOnInsert" -> BSONDocument("player" -> BSONString(a.undo.value.asInstanceOf[String]),"matchId"-> BSONString(matchId),"read" -> BSONBoolean(false)))
      batch = batch :+ delayedUpdate(BSONDocument("_id" -> BSONString(id)),doc,true)
    }
    case ApologyAccepted(matchId,player) => {
      val id: String = matchId + "_" + player
      val update = BSONDocument("$set" -> BSONDocument("read" -> BSONBoolean(true)))
      batch = batch :+ delayedUpdate(BSONDocument("_id" -> BSONString(id)),update,false)
    }

  }

  override def onCommand: Receive = emptyBehavior

}
