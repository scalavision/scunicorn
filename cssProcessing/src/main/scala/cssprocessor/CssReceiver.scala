package cssprocessor

import java.net.{InetSocketAddress}
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.{Executors, TimeUnit}
import cats.effect.{IO, Async}
import fs2._
import fs2.{Sink, Stream, async, text}

import scala.concurrent.ExecutionContext.Implicits.global

class CssReceiver {
  import Resources._

  implicit val AG: AsynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(
    2, Executors.defaultThreadFactory()
  )

  val log: Sink[IO, String] = _.evalMap { text =>
    IO { println(text) }
  }

  val port = new InetSocketAddress("127.0.0.1", 5001)

  val client2 =
    fs2.io.tcp.server[IO](port) //(new InetSocketAddress("127.0.0.1", 5001))

//      .flatMap { s =>
//
//      println("staring up server ...")
//
//      s.flatMap { socket =>
//         Stream.emit(socket.reads(1024)).covary[IO]
//      }
//
//    }

//  val client: Stream[IO, Nothing] =
//    serverWithLocalAddress[IO](
//      new InetSocketAddress(InetAddress.getByName(null), 5001)
//    ).flatMap {
//      case Left(socketAddress) =>
//        Stream.eval_(localBindAddress.setAsyncPure(socketAddress))
//      case Right(stream) =>
//
//        stream.flatMap { socket =>
//          Stream.emit("test").drain
//        }
//
//        stream.evalMap { socket =>
//          IO {
//            socket.reads(1024).through(text.utf8Decode).to(log).drain
//          }
//        }.drain
//
//    }

  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(): Unit = {
      println("shutting down ...")
      AG.shutdownNow()
      println("awaiting socket release ...")
      AG.awaitTermination(10, TimeUnit.SECONDS)
      println("socket released")
    }
  })

}
