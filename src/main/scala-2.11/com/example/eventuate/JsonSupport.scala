package com.example.eventuate

import java.text.SimpleDateFormat
import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.Try

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit object DateFormat extends JsonFormat[Date] {
      def write(date: Date) = JsString(dateToIsoString(date))
      def read(json: JsValue) = json match {
        case JsString(rawDate) => parseIsoDateString(rawDate) get
      }
    }

    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
      override def initialValue() = new SimpleDateFormat("dd/MM/yyyy")
    }

    private def dateToIsoString(date: Date) =
      localIsoDateFormatter.get().format(date)

    private def parseIsoDateString(date: String): Option[Date] =
      Try{ localIsoDateFormatter.get().parse(date) }.toOption

  implicit val matchFormat: RootJsonFormat[Match] = jsonFormat5(Match)

  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat1(Player)

  implicit val apAcFormat: RootJsonFormat[ApologyAcceptance] = jsonFormat2(ApologyAcceptance)

}
