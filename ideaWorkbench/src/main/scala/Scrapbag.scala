import java.nio.file.Paths

import cats.effect.IO
import fs2.{Chunk, Pipe, Pull, Pure, Segment, Stream, io, text}

object Scrapbag {

  /*
  println("hello world")

  val s = Stream(1,2,4)

  val out: Stream[Pure, IO[Unit]] = s.chunks.map { n =>  IO { println(n) } }

  //  val socket = fs2.io.tcp.client(
  //    ???
  //  )


  //  def cssRaw[F[_]]: Stream[F, Byte] = io.file.readAll[IO](Paths.get("out.css"), 16)

  val src = io.file.readAll[IO](Paths.get("simple.css"), 16)

  def linesFromString(string: String): (Vector[String], String) = {
    var i = 0
    var start = 0
    var out = Vector.empty[String]
    while (i < string.size) {
      string(i) match {
        case '\n' =>
          out = out :+ string.substring(start, i)
          start = i + 1
        case '\r' =>
          if (i + 1 < string.size && string(i + 1) == '\n') {
            out = out :+ string.substring(start, i)
            start = i + 2
            i += 1
          }
        case c =>
          ()
      }
      i += 1
    }
    val carry = string.substring(start, string.size)
    (out, carry)
  }

  //  def cssBlocks(string: String): (Vector[String], String) = {
  //    var i = 0
  //    var start = 0
  //    var out= Vector.empty[String]
  //    while (i < string.size) {
  //      string(i) match {
  //        case '{' =>
  //          out = out :+ string.substring(start, i)
  //      }
  //    }
  //
  //    ???
  //  }

  def write(out: Stream[IO, String]) = {
    out.through(text.utf8Encode).through(
      io.file.writeAll[IO](Paths.get("css.css"))
    )
  }

  /*
  Pull.output1(a).flatMap {_ =>
            go(i - 1)(nextStream.pull)
          }

          Can be simplified to:

          Pull.outout1(1) >> go(i-1)(nextStream.pull)
   */

  def myTake[F[_], A](i: Int) = {

    def go[F[_], A](i: Int): Stream.ToPull[F, A] => Pull[F, A, Unit] = h => {
      if(i == 0 ) Pull.done
      else h.uncons1.flatMap {
        case None => Pull.pure(())
        case Some((a,nextStream)) =>
          Pull.output1(a) >> go(i - 1)(nextStream.pull)
      }
    }
    (in: Stream[F,A]) => go(i)(in.pull).stream
  }

  //  var i = 0
  //  var start = 0
  //  var out= Vector.empty[String]
  //  while (i < string.size) {
  //    string(i) match {
  //      case '{' =>
  //        out = out :+ string.substring(start, i)
  //    }
  //  }

  def myTakeC[F[_], A](i: Int) =  {

    def go[F[_], A](i: Int): Stream.ToPull[F, A] => Pull[F, A, Unit] = h => {
      if(i == 0 ) Pull.done
      else h.uncons.flatMap {
        case None => Pull.pure(())
        case Some((as, restOfStream)) =>
          val size = as.toChunks.toList.size
          if(size <= i) Pull.output(as) >> go(i - size)(restOfStream.pull)
          else {
            val rest = as.take(i)
            Pull.output(Segment(as.toVector :_*))
          }
      }
    }
    (in: Stream[F,A]) => go(i)(in.pull).stream
  }


  sealed trait CssBlock {
    def handleChunks(chunk: fs2.Chunk[String]) =
      this match {
        case InitCss =>
          val startBlock = chunk.indexWhere(_ == "{")
          val endBlock = chunk.indexWhere(_ == "}")
          (startBlock, endBlock) match {
            case (Some(s), Some(e)) =>
              CssComplete(Chunk(chunk.toString.drop(s).dropRight(e)))
            case _ => CssComplete(Chunk("Out of synch"))
          }
        case _ => CssComplete(Chunk("Out of synch"))
      }
  }
  case object InitCss extends CssBlock
  case class CssPartial(depth: Int) extends CssBlock
  case class CssComplete(chunk: Chunk[String]) extends CssBlock

  object CssBlock {

  }

  def test[F[_], String]: Pipe[F, String, String] = {
    def go(cssBlock: CssBlock): Stream.ToPull[F, String] => Pull[F, String, Unit] = h => {
      h.unconsChunk.flatMap {
        case None => Pull.pure(())
        case Some((cssText, nextHandle)) =>
          val startBlock = cssText.indexWhere(_ == "{")
          val endBlock = cssText.indexWhere(_ == "}")
          val test: String = Stream.emit("etst")
          Pull.output(Segment(Vector("hello")))


        //          (startBlock, endBlock) match {
        //            case (Some(s), Some(e)) =>
        //              Pull.output(Segment(Vector("full"))) // >> go(InitCss)(nextHandle.pull)
        //            case _ => Pull.output(Segment(Vector("empty"))) // >> go(InitCss)(nextHandle.pull)
        //          }
      }
    }

    (in: Stream[F, String]) => go(InitCss)(in.pull).stream
  }

  val css =
    src.through(text.utf8Decode)
      .filter(!_.trim.isEmpty)
      .through(test)


  //  val css =
  //    src.through(text.utf8Decode)
  //      .through(text.lines)
  //      .filter(!_.trim.isEmpty)
  //      .through(myTakeC(3))

  pprint.pprintln(css.runLog.unsafeRunSync())


  //write(css).run.unsafeRunSync()
  */


  // Tcp client example from the Specs :

  /*


  val localBindAddress = async.ref[IO, InetSocketAddress].unsafeRunSync()

  val clientCount = 1

  val clients = {
    Stream.range(0, clientCount).covary[IO].map { idx =>
      Stream.eval(localBindAddress.get).flatMap { local =>
        tcp.client[IO](local).flatMap { socket =>
          css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
            socket.reads(1024, None).chunks.map(_.toArray)
        }
      }
    }.join(1)
  }



  //    Stream.eval(localBindAddress.get).flatMap { local =>
//      tcp.client[IO]( local ).flatMap { socket =>
//        css.covary[IO].to(socket.writes()).drain.onFinalize(socket.endOfOutput) ++
//          socket.reads(1024, None).chunks.map(_.toArray)
//      }
//    }
   */

  // Example using Sinc:

  /*

/*
val q = {
      val res = for {
        a <- server[Task](skt)
        b <- Stream.emit(a.flatMap(socket =>
socket.reads(1024).to(socket.writes()).onFinalize(socket.endOfOutput)))
      } yield b
      concurrent.join(Int.MaxValue)(res)
    }

    println("about to start ...")
    q.drain.runLog.unsafeRun()
 */

/*
val signal = Signal[Task, Boolean](false).flatMap { sig =>
  val server: Stream[Task, Message] = UdpServer.flow(Stream.emit(feeds), Some(sig))
  val client: Stream[Task, Unit] = sig.discrete.changes.flatMap(_ =>
    fs2.io.udp.open[Task]().evalMap {
      _.write(Packet(new InetSocketAddress("localhost", 1234), Chunk.bytes("this is the application".getBytes)))
    }
  )
  server.mergeDrainR(client).take(1).runLog
}.unsafeRun()
 */

   */
}
