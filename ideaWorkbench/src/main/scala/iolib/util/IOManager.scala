package iolib.util

import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import fs2.Scheduler
import fs2.io.tcp
import iolib.util.Resources.mkThreadFactory

import scala.concurrent.ExecutionContext

object IOManager {

//  implicit val EC: ExecutionContext = ExecutionContext.fromExecutor(
//    Executors.newFixedThreadPool(
//      8, mkThreadFactory(
//        "fs2-http-spec-ec", daemon = true
//      )
//    )
//  )
//
//  implicit val Sch: Scheduler = Scheduler.fromScheduledExecutorService(
//    Executors.newScheduledThreadPool(
//      4, mkThreadFactory("fs2-http-spec-scheduler", daemon = true)
//    )
//  )
//
//  implicit val AG: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(
//    Executors.newCachedThreadPool(
//      mkThreadFactory("fs2-http-spec-AG", daemon = true)
//    )
//  )
//
//  def postCss() = {
//    tcp.client(
//      new InetSocketAddress("127.0.0.1", 5000)
//    ).flatMap { socket =>
//
//
//      ???
//    }

//  }




}
