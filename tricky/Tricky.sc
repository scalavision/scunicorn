import $ivy.`co.fs2::fs2-core:0.10.0-M6`
import $ivy.`co.fs2::fs2-io:0.10.0-M6`
import $ivy.`com.spinoco::fs2-http:0.2.0-SNAPSHOT`

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import java.net.{InetAddress, InetSocketAddress}
import java.util.concurrent.TimeUnit

import fs2._
import fs2.io.tcp._
import cats.effect.IO
import fs2.async
import fs2.async.Ref

import scala.concurrent.duration._

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicInteger

import fs2._
import fs2.interop.scodec.ByteVectorChunk
import scodec.bits.{BitVector, ByteVector}
import scodec.bits.Bases.{Alphabets, Base64Alphabet}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import spinoco.fs2.http.util.mkThreadFactory

def mkThreadFactory(name: String, daemon: Boolean, exitJvmOnFatalError: Boolean = true): ThreadFactory = {
    new ThreadFactory {
      val idx = new AtomicInteger(0)
      val defaultFactory = Executors.defaultThreadFactory()
      def newThread(r: Runnable): Thread = {
        val t = defaultFactory.newThread(r)
        t.setName(s"$name-${idx.incrementAndGet()}")
        t.setDaemon(daemon)
        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
          def uncaughtException(t: Thread, e: Throwable): Unit = {
            ExecutionContext.defaultReporter(e)
            if (exitJvmOnFatalError) {
              e match {
                case NonFatal(_) => ()
                case fatal => System.exit(-1)
              }
            }
          }
        })
        t
      }
    }
  }

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

  lazy val localBindAddress: Ref[IO, InetSocketAddress] =
    async.ref[IO, InetSocketAddress].unsafeRunSync()

  lazy val log: Sink[IO, String] = _.evalMap { text =>
    IO { println(text) }
  }

  lazy val logger: Pipe[IO, String, Byte] = _.flatMap { txt =>
    Stream.eval(IO { println(txt); txt }).through(text.utf8Encode)
  }

  lazy val cssReceiver: Stream[IO, Nothing] =
    serverWithLocalAddress[IO](new InetSocketAddress(InetAddress.getByName(null), 5001)).flatMap {
      case Left(socketAddress) => 
        println("listening on socket ..")
        Stream.eval_(localBindAddress.setAsyncPure(socketAddress))
      case Right(stream) =>
        println("recieving stream...")
        stream.evalMap { socket =>
          IO {
            socket.reads(1024).through(text.utf8Decode).through(logger).to(socket.writes())
          }
        }.drain

    }.joinUnbounded

  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(): Unit = {
      println("shutting down ...")
      AG.shutdownNow()
      println("awaiting socket release ...")
      AG.awaitTermination(10, TimeUnit.SECONDS)
      println("socket released")
    }
  })

  println("starting up ...")
  Thread.sleep(1000)
  println("ready ...")
  cssReceiver.run.unsafeRunTimed(30.minutes)
