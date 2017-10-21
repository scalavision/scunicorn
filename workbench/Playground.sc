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


  implicit val tcpACG : AsynchronousChannelGroup = 
    AsynchronousChannelProvider.provider()
      .openAsynchronousChannelGroup(
        8, 
        ThreadFactories.named("fs2-ag-tcp", true)
      )
/*
def styles[F[_]](implicit F: Sync[F]) = //: F[Unit] = 
   io.file.readAll[F](
     Paths.get("./out.css"), 4096
   )
*/

/*
def client[F[_]](
    implicit F: Effect[F],
  ): Stream[F, Unit] = io.tcp.client(
  to = new InetSocketAddress("127.0.0.1", 5000),
  noDelay = true
).flatMap { socket =>
  println("running inside the socket ???")
  //styles[F].covary[F].writeOutputStream(socket.writes())

  styles[F].covary[F].chunks.map(socket.writes()).drain.onFinalize(socket.endOfOutput)

  styles[F].covary[F].chunks.map {
    case ch: Chunk[Byte] =>
      println(ch)
      socket.write(ch)
  }.drain.onFinalize(socket.endOfOutput)
  
  
  //styles[F].covary[F].to(socket.writes())
    
//    .to( byte => 
//    socket.writes(byte)
//  ).drain
// Stream.chunk(styles[F]).covary[IO].to(socket.writes()) 
 //Stream.chunk("Hello World").covary[IO].to(socket.writes()) 

}*/

println("running")

//val c = client[IO].run.unsafeRunAsync(println)//.run.unsafeRunSync()
//c.unsafeRunSync()
//println("socket shutting down")

//pprint.pprintln(c)

//tcpACG.shutdownNow

