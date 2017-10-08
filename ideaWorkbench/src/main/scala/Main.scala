
import fs2._
import cats.effect.{IO, Effect, Sync}

object Main extends App {

  println("hello world")

  val s = Stream(1,2,4)

  val out: Stream[Pure, IO[Unit]] = s.chunks.map { n =>  IO { println(n) } }

  val socket = fs2.io.tcp.client(
    ???
  )
  
  pprint.pprintln(s)

}