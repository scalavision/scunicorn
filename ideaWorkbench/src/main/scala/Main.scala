
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
import scala.util.Try

object Main extends App {

private val utf8Charset = Charset.forName("UTF-8")

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

  val src: Stream[IO, Byte] = io.file.readAll[IO](Paths.get("simple.css"), 16)

  val buf = new scala.collection.mutable.ListBuffer[String]()

  def delay[A](sleepTime: FiniteDuration): Pipe[IO, A, A] = _.flatMap { a =>
    Sch.delay(Stream.eval(IO(a)), sleepTime)
  }

  val css =
    src.through(text.utf8Decode)
      .through(cssBlocks)
      .through(text.utf8Encode)

  val localBindAddress = async.ref[IO, InetSocketAddress].unsafeRunSync()

  def postProcessCss(s: Stream[IO, Byte]): Stream[IO, Byte] =
    tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000)).flatMap { socket =>
      s.to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++ socket.reads(1024, None)
    }

//  def pC: Pipe[IO, Chunk[Byte], Byte] = _.evalMap { c =>
//
//    IO {
//      tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000)).flatMap { socket =>
//        socket.write(c); socket.reads(1024, None)
//      }
//    }
//  }

//  def postProcessCssOld2: Pipe[IO, String, Byte] = _.flatMap { (s: String) =>
//
//    tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000)).flatMap { socket =>
//      Stream.emit(socket.write(Chunk.array(s.getBytes))) ++ socket.reads(1024, None)
//    }
//  }
//
//  val client = tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000))

//  val cssPush: Pipe[IO, String, String] = _.evalMap { s =>
//    cssBlockQueue.evalMap { q => q.enqueue1(s) }
//    IO { s }
//  }

  val queue = Stream.eval(async.unboundedQueue[IO, String])

  def pushCss(s: String): Stream[IO, Unit] = for {
    q <- queue
    _ <- Stream(s).covary[IO].to(q.enqueue)
    _ <- q.dequeue.through(log("testi t: ")).drain
  } yield ()

  val monitor: Stream[IO, Nothing] = queue.flatMap { q =>
    println("inside monitor ...")
    q.dequeue.through(log("monitor of queue: >")).drain
  }

  val pushHelper: Pipe[IO, String, String] = _.flatMap { s =>
    println("helping with push ...")
    IO { pushCss(s)} ; Stream.eval(IO(s))
  }

  val echoServer: Stream[IO, Byte] =
    serverWithLocalAddress[IO](new InetSocketAddress(InetAddress.getByName(null), 5001)).flatMap {
      case Left(local) => Stream.eval_(localBindAddress.setAsyncPure(local))
      case Right(s) =>
        // socket.reads(1024).to(socket.writes()).onFinalize(socket.endOfOutput)

        Stream.emit(s.flatMap { socket =>

//          val rrr: Stream[IO, Byte] = socket.reads(1024)
//            .through(text.utf8Decode andThen cssBlocks)
//            .through(log("after blocks"))
//            .through(text.utf8Encode)
//
//          val result: Stream[IO, Byte] = postProcessCss(rrr)

          for {
            css <- socket.reads(1024).through(text.utf8Decode andThen cssBlocks)
            _ <- Stream(css).covary[IO].through(log("info: "))
            cssProcessed <- postProcessCss(Stream(css).through(text.utf8Encode))
            _ = println(cssProcessed)
            _ <- Stream(cssProcessed).covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput)
          } yield cssProcessed

//          println(result.through(log("result: ")).runLog.unsafeRunSync())
//
//          result.to(socket.writes()).drain.onFinalize(socket.endOfOutput)

        })

    }.joinUnbounded

  val cssClient: Stream[IO, Byte] =
      tcp.client[IO]( new InetSocketAddress("127.0.0.1", 5000) ).flatMap { socket =>
        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
          socket.reads(1024, None)
      }

//  val program =
//    cssClient.through(text.utf8Decode).through(cssBlocks).to(logProcessedCss)

  val program = echoServer

//  val shutdown: Sink[IO, Unit] = _.evalMap { s =>
//    IO { AG.shutdownNow() }
//  }

  program.run.unsafeRunSync()

//  program.run.unsafeRunAsync(println)

  def log[A](prefix: String): Pipe[IO, A, A] =
    _.evalMap{ s => IO { println(s"$prefix" + s.toString);s}}



}