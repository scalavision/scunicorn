package workbench

import fs2._
import cats.effect.{Effect, IO, Sync}
import java.util.concurrent.Executors
import iolib.util.Resources.mkThreadFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object SignalsAndQueues {

  implicit val EC: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(
      8, mkThreadFactory(
        "fs2-http-spec-ec", daemon = true
      )
    )
  )

  implicit val Sch: Scheduler = Scheduler.fromScheduledExecutorService(
    Executors.newScheduledThreadPool(
      4, mkThreadFactory("fs2-http-spec-scheduler", daemon = true)
    )
  )

  def log[A](prefix: String): Pipe[IO, A, A] =
    _.evalMap{ s => IO { println(s"$prefix" + s.toString);s}}


  def randomDelays[A](max: FiniteDuration): Pipe[IO, A, A] = _.flatMap { a =>
    Sch.delay(Stream.eval(IO(a)), scala.util.Random.nextInt(max.toMillis.toInt) millis)
    //    Sch.delay(Stream.eval(IO(a)), max)
  }


  val ourSignal = Stream.eval(async.signalOf[IO, Int](0))



  val signalWriterWithMonitoring = ourSignal.flatMap { s =>
    // monitor is only used for 'monitoring', hence, we send data through our log, then
    // drain the result, i.e. removes all data, i.e. turns them into Nothing
    val monitor: Stream[IO, Nothing] = s.discrete.through(log("monitor: >")).drain

    // generating our data in this example
    val data: Stream[IO,Int] = Stream.range(10,20).covary[IO].through(randomDelays(1.second))

    // we take our generated data and update the signal with them.
    // evalMap will do this synchronously? It is generating a stream of Unit
    val writer: Stream[IO, Unit] = data.evalMap{ d => s.set(d)}

    // We need to use merge, since the monitor signal is drain'ed. The stream
    // would stop, if not using merge. We also need to merge in order to be
    // able to make the monitor stream `run` asynchronously?
    // mergeHaltBoth will make the program terminate when one of the streams terminates
    monitor mergeHaltBoth writer
  }

  //TODO: try to make this into the server paradigm ...

  val boundedQueue = Stream.eval(async.boundedQueue[IO, Int](5))

  val boundedQueueWithMonitoring = boundedQueue.flatMap { q =>
    val monitor: Stream[IO, Nothing] = q.dequeue.through(log("monitor of queue: >")).drain
    val data: Stream[IO,Int] = Stream.range(10,20).covary[IO].through(randomDelays(1.second))
    // Enqueue'ing (putting) one element at a time into the queue from our data
    // this pattern is allready available in fs2
    // val writer: Stream[IO, Unit] = data.evalMap { d => q.enqueue1(d)}
    val writer: Stream[IO, Unit] = data.to(q.enqueue)

    monitor mergeHaltBoth writer
  }

  //  signalWriterWithMonitoring.runLog.unsafeRunSync()
  boundedQueueWithMonitoring.runLog.unsafeRunSync()


}
