package cssprocessor

import cats.effect.IO
import fs2._
import spinoco.fs2.http
import http._
import http.websocket._
import spinoco.fs2.http.util._

import scala.concurrent.duration._
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import scodec.Codec
import scodec.codecs.utf8

import scala.concurrent.ExecutionContext

object BrowserUpdater {
  import Resources._

  val clockStream: Stream[IO, Frame.Text[String]] =
    Sch.delay(Stream.eval(IO { Frame.Text("info from server")}), 1.second)

  def serverEcho(s: Stream[IO, Frame[String]]):
    Pipe[IO, Frame[String], Frame[String]] = { in =>
      in.map { _.a }.to(log("serverEcho:")).drain.run.unsafeRunAsync(println)
      s merge in
    }

  private val server =
    http.server[IO](new InetSocketAddress("127.0.0.1", 9092)) _

  private val webSocket = websocket.server(
    pipe = serverEcho(clockStream.repeat),
    pingInterval = 500.millis,
    handshakeTimeout = 10.second
  ) _

  val serverStream: Stream[IO, Unit] = server(webSocket)

}
