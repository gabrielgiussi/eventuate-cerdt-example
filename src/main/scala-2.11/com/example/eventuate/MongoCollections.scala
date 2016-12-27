package com.example.eventuate

import akka.util.Timeout
import reactivemongo.api.{MongoConnection, MongoDriver, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

class MongoCollections(val mongoUri: String,val dbname: String, implicit val executionContext: ExecutionContext) {

  private implicit val timeout = Timeout(20.seconds)

  private val driver = MongoDriver()
  private val parsedUri = MongoConnection.parseURI(mongoUri)
  private val connection = parsedUri.map(driver.connection(_))
  private val futureConnection = Future.fromTry(connection)

  private def db1: Future[DefaultDB] = futureConnection.flatMap(_.database(dbname))
  def apologies: Future[BSONCollection] = db1.map(_.collection("apologies"))
  def progress: Future[BSONCollection] = db1.map(_.collection("progress"))

}
