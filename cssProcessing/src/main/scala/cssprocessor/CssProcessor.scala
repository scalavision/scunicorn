package cssprocessor

import cats.effect.IO
import fs2._
import spinoco.fs2.http
import http._
import http.websocket._
import spinoco.fs2.http.util._

import scala.concurrent.duration._
import java.net.{InetAddress, InetSocketAddress}
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import fs2.io.tcp.serverWithLocalAddress
import fs2.async.Ref
import fs2.io.tcp
import scodec.Codec
import scodec.codecs.utf8

import scala.concurrent.ExecutionContext

object CssProcessor {
  import Resources._
  import CssStreamHandler._

  val localBindAddress: Ref[IO, InetSocketAddress] = async.ref[IO, InetSocketAddress].unsafeRunSync()

  def postProcessCss(s: Stream[IO, Byte]): Stream[IO, Byte] =
    tcp.client[IO](new InetSocketAddress("127.0.0.1", 5000)).flatMap { socket =>
      s.to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++ socket.reads(1024, None)
    }

  val cssProcessor: Stream[IO, Byte] =
    serverWithLocalAddress[IO](new InetSocketAddress(InetAddress.getByName(null), 5001)).flatMap {
      case Left(local) => Stream.eval_(localBindAddress.setAsyncPure(local))
      case Right(s) =>
        Stream.emit(s.flatMap { socket =>
          for {
            css <- socket.reads(1024).through(text.utf8Decode andThen cssBlocks)
            _ <- Stream(css).covary[IO].to(log("info"))
            cssProcessed <- postProcessCss(Stream(css).through(text.utf8Encode))
            // this is not able to run without a runner ... hmm ..???
            //logging = Stream(cssProcessed).covary[IO].through(text.utf8Decode).through(cssBlocks).to(log("processed")).drain
            _ <- Stream(cssProcessed).through(text.utf8Decode).through(cssBlocks).covary[IO].through(pipeLog("returning")).through(text.utf8Encode).to(socket.writes())
            //_ <- Stream(cssProcessed).covary[IO].to(socket.writes())
          } yield cssProcessed
        })
    }.joinUnbounded
}
