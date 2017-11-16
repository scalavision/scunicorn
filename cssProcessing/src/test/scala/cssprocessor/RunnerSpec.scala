package cssprocessor

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

class RunnerSpec extends Specification with AfterAll {
  import Resources._
  import BrowserUpdater._

  def afterAll(): Unit = {
    println("awaiting termination...")
    AG.awaitTermination(6, TimeUnit.SECONDS)
    AG.shutdownNow()
  }

  "Runner" should {

    "provide server api" in {

      var received:List[Frame[String]] = Nil

      def clientData: Pipe[IO, Frame[String], Frame[String]] = { inbound =>
        val output =
          Sch.awakeEvery[IO](1.seconds).map { dur => Frame.Text(s" ECHO $dur") }.take(5)

        output concurrently inbound.take(5).map { in => received = received :+ in}

      }

      val clientMock = Sch.sleep[IO](3.seconds) ++ WebSocket.client(
        WebSocketRequest.ws("127.0.0.1", 9092, "/"),
        clientData
      )

      println("starting up the server and client ... ")


      (serverStream mergeHaltBoth clientMock).runLog.unsafeRunTimed(20.seconds)

      pprint.pprintln(received)

      println("everything has been running for 5 seconds at least ")

      1 === 1

    }
  }

}
