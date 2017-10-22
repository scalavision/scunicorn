
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

/*
val q = {
      val res = for {
        a <- server[Task](skt)
        b <- Stream.emit(a.flatMap(socket =>
socket.reads(1024).to(socket.writes()).onFinalize(socket.endOfOutput)))
      } yield b
      concurrent.join(Int.MaxValue)(res)
    }

    println("about to start ...")
    q.drain.runLog.unsafeRun()
 */

/*
val signal = Signal[Task, Boolean](false).flatMap { sig =>
  val server: Stream[Task, Message] = UdpServer.flow(Stream.emit(feeds), Some(sig))
  val client: Stream[Task, Unit] = sig.discrete.changes.flatMap(_ =>
    fs2.io.udp.open[Task]().evalMap {
      _.write(Packet(new InetSocketAddress("localhost", 1234), Chunk.bytes("this is the application".getBytes)))
    }
  )
  server.mergeDrainR(client).take(1).runLog
}.unsafeRun()
 */

object Main extends App {

private val utf8Charset = Charset.forName("UTF-8")

  val src = io.file.readAll[IO](Paths.get("simple.css"), 16)

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

  val clientCount = 1

  // TODO: try to implement the same function as the HttpClient request method

  val clients = {
    Stream.range(0, clientCount).covary[IO].map { idx =>
      Stream.eval(localBindAddress.get).flatMap { local =>
        tcp.client[IO](local).flatMap { socket =>

//          Stream.emit(css.covary[IO].to(socket.writes()).onFinalize(socket.endOfOutput).run.attempt flatMap {
//            case Left(err) => throw new Exception("failed")
//            case Right(()) => IO.pure(())
//          })

//          css.chunks.map { c =>
//            socket.write(c)
//          }.run.unsafeRunSync()

          css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
            socket.reads(1024, None).chunks.map(_.toArray)
        }
      }
    }.join(1)
  }


  val cssClient: Stream[IO, Array[Byte]] =

      tcp.client[IO]( new InetSocketAddress("127.0.0.1", 5000) ).flatMap { socket =>
        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput)
      }


//    Stream.eval(localBindAddress.get).flatMap { local =>
//      tcp.client[IO]( local ).flatMap { socket =>
//        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
//          socket.reads(1024, None).chunks.map(_.toArray)
//      }
//    }


  //println(Stream(clients).join(1).take(clientCount).runLog.unsafeRunTimed(4 seconds))


  pprint.pprintln(cssClient.run.unsafeRunSync())

}