
import ammonite.ops._

import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.spi.AsynchronousChannelProvider

import fs2._
import fs2.internal.ThreadFactories
//import $ivy.`org.scalaz::scalaz-core:7.2.7`
import $ivy.`co.fs2::fs2-core:0.10.0-M6`
import $ivy.`co.fs2::fs2-io:0.10.0-M6`
import cats.effect.Effect
import cats.effect.{IO, Sync}
import cats.syntax.all._
import fs2._
import fs2.{io, text}
import fs2.io.tcp
import java.nio.file.Paths
import java.net.InetSocketAddress
//import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


val so = fs2.tcp.client(
    new InetSocketAddress("127.0.0.1", 5000)
  )




// val test[F[_]] = Stream(1).runLog.unsafeRun

