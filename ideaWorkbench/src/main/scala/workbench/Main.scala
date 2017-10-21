package workbench


import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import cats.effect.IO
import fs2._
import fs2.io.tcp.Socket

import scala.concurrent.ExecutionContext

/*
object Main extends App {
  import ExecutionContextExecutorServiceBridge._


  implicit val context : ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val group : AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(ExecutionContextExecutorServiceBridge(context))

//  val cssFromFile: Stream[IO, Byte] = io.file.readAll[IO](Paths.get("styles.css"), 4096)

  val address = new InetSocketAddress("127.0.0.1", 5000)
//  val postCss: Stream[IO, Socket[IO]] = io.tcp.client[IO](address)

  //val result = cssFromFile.through(text.utf8Decode).through(text.lines).runLog.unsafeRunSync

//  println(result.size)
//  println(result(10))
//  println(result.mkString("\n"))

//  postCss.flatMap { socket =>

    //socket.reads(16).through(text.utf8Decode).map { t => IO { println(t) }}

//    socket.reads(16).through(io.file.writeAll(Paths.get("out.css")))
//
//  }.run.unsafeRunAsync(println)

//  val css = postCss.flatMap { socket =>
//
//    cssFromFile.bufferAll.covary[IO].flatMap { b =>
//      Stream.eval(socket.write(Chunk(b)))
//    }

//    socket.reads(256 * 1024)
//      .through(fs2.text.utf8Decode)
//      .evalMap(s => IO(println(s)))

//  }.run.unsafeRunSync



//  postCss.flatMap { socket =>
//
//
//  }.run.unsafeRunSync()

  //group.awaitTermination(14, TimeUnit.SECONDS)
//
  //Thread.sleep(2000)


  def mySink[F[_], A](out: Stream[F, A]) = {
    Stream.eval( IO{println(out)} )
  }

//  def myPipe[F[_]](data: F)

  val s = Stream(1,2,3,4)

//  s.to(mySink[IO, Int])

}*/