package com.example.eventuate


object EventuateMatchs {

  def location(id: String, port: Int, mongoUri: String,dbname: String) = new EventuateLocation(id,port,mongoUri,dbname)

  def main(args: Array[String]) = {
    println(args(0))
    println(args(1))
    println(args(2))
    val locationA = location(args(0),Integer.valueOf(args(1)),s"mongodb://${args(2)}:27017/eventuate","eventuate")
  }
}



object EventuateMatchsA extends App {
  //val locationA = location("location-A",8080,"mongodb://eventuate:eventuate@ds129018.mlab.com:29018/eventuate","eventuate")
  //val locationA = location("location-A",8080,"mongodb://mongo:27017/eventuate","eventuate")
  //val locationName:String = ManagementFactory.getRuntimeMXBean.getInputArguments
  val localtion = new EventuateLocation("location-A-dev",8080,"mongodb://localhost:27017/eventuate","eventuate")
}

object EventuateMatchsB extends App {
  //val locationA = location("location-B",8081,"mongodb://eventuate:eventuate@ds129028.mlab.com:29028/eventuate_b","eventuate_b")
  //val locationA = location("location-B",8080,"mongodb://localhost:27018/eventuate_b","eventuate_b")
  val localtion = new EventuateLocation("location-B-dev",8081,"mongodb://localhost:27018/eventuate","eventuate")
}