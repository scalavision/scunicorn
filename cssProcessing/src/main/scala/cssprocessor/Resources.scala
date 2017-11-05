package cssprocessor

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import cats.effect.IO
import fs2.{Pipe, Scheduler, Sink}
import scodec.Codec
import scodec.codecs.utf8
import spinoco.fs2.http.util.mkThreadFactory

import scala.concurrent.ExecutionContext

object Resources {

  implicit val codecByte: Codec[String] = utf8

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

  def log(prefix: String): Sink[IO, String] = _.evalMap { s =>
    IO(println(s"$prefix > " + s))
  }

  def pipeLog(prefix: String): Pipe[IO, String, String] = _.evalMap { s =>
    println(s"$prefix > $s") ; IO(s)
  }


}
