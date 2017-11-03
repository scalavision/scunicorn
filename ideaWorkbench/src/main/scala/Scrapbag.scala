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


  /**

  Signals and stuff



  val a = Stream.range(0,10).covary[IO].through(randomDelays(1 second)).through(log("A"))
  val b = Stream.range(0,10).covary[IO].through(randomDelays(2 seconds)).through(log("B"))
  val c = Stream.range(0,10).covary[IO].through(randomDelays(1 seconds)).through(log("C"))

  // A will allways wait for B before continueing
//  val program = (a interleave b).through(log("interleaved"))

//  val program = (a merge b).through(log("merged"))

  //val program = (a either b).through(log("either"))

  // Stream of Streams:
  // val streams: Stream[IO, Stream[IO, Int]] = Stream(a,b,c)

  // Ten streams, with inner streams of 10 items running concurrently
  val streams = Stream.range(0,10).map { id =>
    Stream.range(1,10).covary[IO].through(randomDelays(1 second)).through(log(('A' + id).toChar.toString))
  }

  // Running only three streams at the same time
//  val program = streams.join(3).through(log("joined"))
//  program.run.unsafeRunSync()


  val x = async.signalOf[IO, Int](1)

  // To get the signal wrapped in an effect
  val exe: IO[Int] = x.flatMap { x1: Signal[IO,Int] => x1.get }

  // we can also create a single element stream, using eval
  val s1: Stream[IO, Signal[IO, Int]] = Stream.eval(x)

  // create a signal and return all changes that has been done to this signal
  val s1Listen: Stream[IO, Int] = Stream.eval(x).flatMap { x1 => x1.discrete }

  // IO describes a computation, IO describes a way to generate the wanted value (most oftenly?) using a side effect)

  // Cheating to demonstrate the way signal works, let's do the unsafeRun to get our signal
  val signal = x.unsafeRunSync()

  signal.discrete.through(log("signal > ")).run.unsafeRunAsync(println)

  pprint.pprintln(signal.set(2).unsafeRunSync())

  pprint.pprintln(signal.modify(_ + 1).unsafeRunSync())

  pprint.pprintln(signal.continuous.take(5))

  // It will be signaled on every write to the value
  signal.set(3).unsafeRunSync()
  signal.set(4).unsafeRunSync()
  signal.set(5).unsafeRunSync()

  //program.run.unsafeRunAsync(println)
  println("hello world finished")

  */

}
