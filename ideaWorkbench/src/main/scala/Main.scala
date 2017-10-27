
import java.net.{InetAddress, InetSocketAddress}
import java.nio.channels.AsynchronousChannelGroup
import java.nio.file.Paths

import fs2._
import cats.effect.{Effect, IO, Sync}
import java.nio.charset.Charset
import java.util.concurrent.Executors

import css.CssStreamHandler._
import fs2.async.mutable.Signal
import fs2.io.tcp
import fs2.io.tcp.serverWithLocalAddress
import iolib.util.Resources.mkThreadFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {

private val utf8Charset = Charset.forName("UTF-8")

  val src: Stream[IO, Byte] = io.file.readAll[IO](Paths.get("simple.css"), 16)

  val buf = new scala.collection.mutable.ListBuffer[String]()

  val css =
    src.through(text.utf8Decode)
      .through(cssBlocks)
      .through(text.utf8Encode)

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

  implicit val AG: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(
    Executors.newCachedThreadPool(
      mkThreadFactory("fs2-http-spec-AG", daemon = true)
    )
  )

  val localBindAddress = async.ref[IO, InetSocketAddress].unsafeRunSync()

  def postProcessCss(s: Stream[IO, Byte]): Stream[IO, Byte] =
    tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000)).flatMap { socket =>
      s.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
        socket.reads(1024, None)
    }

  val cssStream: IO[Signal[IO, String]] = async.signalOf[IO, String]("")


 def cssPush: Pipe[IO, String, String] = ???
//   _.evalMap { s => cssStream.flatMap { so => so.discrete.throughPure(logPipe)} }


   //_.evalMap { a => IO { println(a); a }}

  val echoServer: Stream[IO, Byte] = {
    serverWithLocalAddress[IO](new InetSocketAddress(InetAddress.getByName(null), 5001)).flatMap {
      case Left(local) => Stream.eval_(localBindAddress.setAsyncPure(local))
      case Right(s) =>
        Stream.emit(s.flatMap { socket =>
          socket.reads(1024)
            .through(text.utf8Decode)
            .through(cssBlocks)
            .through(text.utf8Encode)
            .through(postProcessCss)
            .through(text.utf8Decode)
            .through(logPipe)
            .through(cssPush)
            .drain.onFinalize(socket.endOfOutput)
        })
    }.joinUnbounded
  }

  val cssClient: Stream[IO, Byte] =
      tcp.client[IO]( new InetSocketAddress("127.0.0.1", 5000) ).flatMap { socket =>
        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
          socket.reads(1024, None)
      }

//  val cssQueue = async.signalOf[IO, Byte](echoServer)(EC)

  val logProcessedCss: Sink[IO, String]  =
    _.evalMap { a => IO { println(a) } }

  val logPipe : Pipe[IO, String, String] =
    _.evalMap { a => IO { println(a); a }}

//  val program = cssClient.through(text.utf8Decode).through(cssBlocks).to(logProcessedCss)

//  val program = echoServer

//  program.run.unsafeRunSync()

  def log[A](prefix: String): Pipe[IO, A, A] =
    _.evalMap{ s => IO { println(s"$prefix" + s.toString);s}}


  def randomDelays[A](max: FiniteDuration): Pipe[IO, A, A] = _.flatMap { a =>
    Sch.delay(Stream.eval(IO(a)), max)
  }

  val program =
    Stream(1,2,3)
      .covary[IO]
      .through(log("> "))
      .through(randomDelays(5.second))
      .through(log("finished: "))

  program.run.unsafeRunAsync(println)

}