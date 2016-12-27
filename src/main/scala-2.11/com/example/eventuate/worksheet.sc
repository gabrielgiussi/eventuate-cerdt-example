import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

// the following is equivalent to `implicit val ec = ExecutionContext.global`
import scala.concurrent.ExecutionContext.Implicits.global

val f = Future {
  throw new Exception()
}

Future (6 / 0) recover { case e: ArithmeticException => 0 } // result: 0
Future (6 / 2) recover { case e: ArithmeticException => 0 } // result: 3


val f2 = Future { Int.MaxValue }
val f3 = Future (6 / 0) recoverWith { case e: ArithmeticException => f2 }
val res = f3 onComplete{
  case Success(e) => "Success"
  case Failure(t) => "Failure"
}

Await.result(f3, 10 seconds)


