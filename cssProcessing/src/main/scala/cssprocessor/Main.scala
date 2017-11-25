package cssprocessor

import java.net.{InetAddress, InetSocketAddress}
import java.util.concurrent.TimeUnit

import fs2._
import fs2.io.tcp._
import cats.effect.IO
import fs2.async
import fs2.async.Ref

import scala.concurrent.duration._

object Main {

  val cssReceiver = new CssReceiver()

  def main(args: Array[String]): Unit = {
    println("starting up ...")
    Thread.sleep(1000)
    println("ready ...")
    //cssReceiver.client2.run.unsafeRunTimed(30.minutes)
    println("error")
  }

}
