package com.example.eventuate


class EventuateMatchs extends App {
  def location(id: String, port: Int, mongoUri: String,dbname: String) = new EventuateLocation(id,port,mongoUri,dbname)
}




object EventuateMatchsA extends  EventuateMatchs {
  val locationA = location("location-A",8080,"mongodb://eventuate:eventuate@ds129018.mlab.com:29018/eventuate","eventuate")
}

object EventuateMatchsB extends  EventuateMatchs {
  val locationA = location("location-B",8081,"mongodb://eventuate:eventuate@ds129028.mlab.com:29028/eventuate_b","eventuate_b")
}