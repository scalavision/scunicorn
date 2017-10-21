
import $ivy.`co.fs2::fs2-core:0.10.0-M6`
import $ivy.`co.fs2::fs2-io:0.10.0-M6`

import cats.effect.{IO, Effect, Sync, Async}
import fs2._

import $file.Executor
import Executor._
import java.nio.file.Paths
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import java.util.concurrent.{ AbstractExecutorService, TimeUnit }
import java.util.Collections

object ExecutionContextExecutorServiceBridge {
  def apply(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
    case null => throw null
    case eces: ExecutionContextExecutorService => eces
    case other => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = other
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown() = ()
      override def shutdownNow() = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = other execute runnable
      override def reportFailure(t: Throwable): Unit = other reportFailure t
      override def awaitTermination(length: Long,unit: TimeUnit): Boolean = false
    }
  }
}

val file = io.file.readAll[IO](Paths.get("styles.css"), 4096)

val address = new InetSocketAddress("127.0.0.1", 5000)

import ExecutionContextExecutorServiceBridge._

implicit val context : ExecutionContext = scala.concurrent.ExecutionContext.global
implicit val group : AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(ExecutionContextExecutorServiceBridge(context))

val postCss = io.tcp.client[IO](address)

val program = file.through(IO { println } ).map { line => postCss.flatMap { socket => Stream.eval(socket.write(Chunk(line))) }}
