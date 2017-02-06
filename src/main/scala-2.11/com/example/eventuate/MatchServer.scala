package com.example.eventuate

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.util.Timeout
import com.example.eventuate.EducatedActor.AcceptApology
import com.example.eventuate.MatchActor.{AddPlayer, CreateMatch, CreateMatchSuccess}
import com.example.eventuate.MatchView.{GetMatch, GetMatchSuccess, GetMatchs, GetMatchsSuccess}
import reactivemongo.bson.{BSONBoolean, BSONDocument, BSONString}


import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

class MatchServer(val port: Int,implicit val system: ActorSystem,implicit val mat: ActorMaterializer, implicit val executionContext: ExecutionContext,val manager: ActorRef,val view: ActorRef, val apologiesSource: Source[EducatedActor.MatchApology,ActorRef],val educatedActor: ActorRef, val mongoCollections: MongoCollections) extends Directives with JsonSupport {

  private implicit val timeout = Timeout(20.seconds)
  import akka.http.scaladsl.model.MediaTypes.`application/json`
  import akka.http.scaladsl.server.directives.ContentTypeResolver.Default

  val addPlayer = path("match" / JavaUUID / "player") { id =>
    post {
      entity(as[Player]) { p =>
        val f = manager ? AddPlayer(id.toString, p.player)
        onComplete(f) {
          case Success(a) => complete(HttpEntity(`application/json`,"{msg: 'Player Created'}"))
          case Failure(e) => complete(500,HttpEntity(`application/json`,s"{err: ${e.getMessage}"))
        }
      }
    }
  }

  val matchRoute = path("match" / JavaUUID) { id =>
    get {
      onSuccess(view ? GetMatch(id.toString)) {
        case GetMatchSuccess(id, m) => {
          complete(m)
        }
        case _ => complete("No existe")
      }
    }
  }

  val matchs = pathPrefix("match") {
    pathEnd {
      get {
        onSuccess(view ? GetMatchs()) { res =>
          val l: List[Match] = res.asInstanceOf[GetMatchsSuccess].matchs.toList
          complete(listFormat[Match].write(l))
        }
      } ~
      post {
        entity(as[Match]) { m =>
          val f: Future[Option[Match]] = manager ? CreateMatch(java.util.UUID.randomUUID().toString, m.creator, m.date, m.place) map (created => Some(created.asInstanceOf[CreateMatchSuccess].m)) recover { case _ => None }
          onSuccess(f) {
            case Some(m) => complete(m)
            case None => complete(500, "No se pudo crear el partido")
          }
        }
      }

    }
  }

  def myFlow(player: String) = {
    println("Player: " + player)
    // TODO Que rol cumple Future[State]
    val query: Future[Source[BSONDocument, NotUsed]] = for {
      ap <- mongoCollections.apologies
      //results <- ap.find(BSONDocument("player" -> BSONString(player))).cursor[BSONDocument]().collect[List](50)
      results <- ap.find(BSONDocument("player" -> BSONString(player), "read" -> BSONBoolean(false))).cursor[BSONDocument]().collect[List](50)
    } yield Source.fromIterator[BSONDocument](() => results.iterator)

    query map {
      case fromQuery: Source[BSONDocument, NotUsed] => {
        val a:Source[String,NotUsed] = fromQuery map { _.getAs[String]("matchId").get }
        val a2: Source[String,ActorRef] = apologiesSource filter {_.removed equals player} map { _.matchId }
        // TODO Porqué puedo hacer un merge de 2 sources con distinto parameter Source[String,<distinto>]
        val a3 = a merge a2
        //val apologies:Source[String,NotUsed] = fromQuery map {_.getAs[String]("matchId").get } merge (apologiesSource filter {_.removed equals player} map {_.matchId})
        //Flow.fromSinkAndSource(Sink.ignore, a3 map {x => println(x); x} filter {_ equals player} map {d => TextMessage.Strict("Yo have been removed from: " + d)})
        val sink = Sink.foreach[Message] { case TextMessage.Strict(data) => {
          // TODO Cómo mejorar este codigo?
          println("Recibido via WS: "+data)
          val parsed = scala.util.parsing.json.JSON.parseFull(data) match {
            case Some(c:Map[String,Any]) => educatedActor ! AcceptApology(c.get("matchId").get.toString,c.get("player").get.toString)
            case None => println("Error de parseo")
          }
          }
        }
        Flow.fromSinkAndSource(sink, a3 map {x => println(x); x} map {d => TextMessage.Strict("{ \"matchId\":\"" +d +"\"}") })
      }
    }
  }


  val apologies =
    path("apologiesWS") {
      parameter('name) { name ⇒
        get {
          onComplete(myFlow(name)){
            case Success(f) => handleWebSocketMessages(f)
            case Failure(e) => throw e
          }
        }
      }
    }

  val route =
    get {
      pathSingleSlash {
        getFromResource("build/index.html")
      } ~
      pathPrefix("static" / "js"){
        getFromResourceDirectory("build/static/js")
      }
    } ~ matchs ~ matchRoute ~ addPlayer ~ apologies


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


}
