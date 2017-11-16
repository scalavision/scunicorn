package cssprocessor

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit

import org.specs2.mutable.Specification
import cats.effect.IO
import fs2._
import spinoco.fs2.http
import http._
import http.websocket._
import spinoco.protocol.http.header._
import spinoco.protocol.http._
import spinoco.protocol.http.header.value._
import http.websocket._
import org.specs2.specification.AfterAll
import spinoco.fs2.http.util._
import spinoco.protocol.http.Uri

import scala.concurrent.duration._
import cats.effect.IO
import fs2.io.tcp
import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scala.concurrent.duration._

class CssProcessorSpec extends Specification with AfterAll {
  import Resources._
  import CssProcessor._

  def afterAll(): Unit = {
    println("shutting down ...")
    AG.shutdownNow()
    println("awaiting termination...")
    AG.awaitTermination(16, TimeUnit.SECONDS)
    ()
  }

  val sample1: String =
    s"""
       |:display a { display: flex; }
     """.stripMargin

  TODO: https://github.com/scalavision/scunicorn/issues/1

  "CssProcessor" should {
    "process incoming css raw text" in {

      val startServer = cssProcessor

      val cssOutput: Stream[IO, Byte] = {
        Sch.sleep[IO](4.seconds) ++ tcp.client[IO](
          new InetSocketAddress("127.0.0.1", 5001)
        ).flatMap { (socket: io.tcp.Socket[IO]) =>
          Stream(sample1)
            .through(text.utf8Encode).covary[IO]
            .to(socket.writes())
            .drain.onFinalize(socket.endOfOutput) ++
                socket.reads(1024, None)
        }
      }.collect { case b: Byte => b} // stripping away the scheduler sleep unit thingy

      println("starting up the server and sending data  ...")

      (startServer mergeHaltBoth cssOutput.through(
        text.utf8Decode andThen CssStreamHandler.cssBlocks
      ).to(log("result"))).run.unsafeRunTimed(10.seconds)

      println("data processed, finished")

      1 === 1

    }
  }

}
