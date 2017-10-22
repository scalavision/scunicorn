
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.nio.file.Paths

import fs2._
import cats.effect.{Effect, IO, Sync}
import java.nio.charset.Charset
import java.util.concurrent.Executors

import css.CssStreamHandler._
import fs2.io.tcp
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

  val cssServer: Stream[IO, Byte] =
    tcp.server[IO](new InetSocketAddress("127.0.0.1", 5001).flatMap { socket =>

      ???

    }

  val cssClient: Stream[IO, Byte] =
      tcp.client[IO]( new InetSocketAddress("127.0.0.1", 5000) ).flatMap { socket =>
        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
          socket.reads(1024, None)
      }

  val logProcessedCss: Sink[IO, String]  =
    _.evalMap { a => IO { println(a) } }

  val program = cssClient.through(text.utf8Decode).through(cssBlocks).to(logProcessedCss)

  program.run.unsafeRunSync()





}