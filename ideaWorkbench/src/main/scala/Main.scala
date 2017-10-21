
import java.nio.file.Paths

import fs2._
import cats.effect.{Effect, IO, Sync}
import java.nio.charset.Charset

import scala.annotation.tailrec

object Main extends App {

private val utf8Charset = Charset.forName("UTF-8")

//  val out: Stream[Pure, IO[Unit]] = s.chunks.map { n =>  IO { println(n) } }

  val src = io.file.readAll[IO](Paths.get("simple.css"), 16)

  /** Transforms a stream of `String` such that each emitted `String` is a line from the input. */
  def cssBlocks[F[_]]: Pipe[F, String, String] = {

    def linesFromString(string: String, foundSoFar: String): (Vector[String], String) = {
      var i = 0
      var start = 0
      var out = Vector.empty[String]
      while (i < string.size) {
        string(i) match {
//          case '{' =>
//            println("found one ..." + i)
//            ()
          case '}' =>
            val nrOfOpenBlock =
              foundSoFar.count(_ == '{') +
                string.substring(start, i).count(_ == '{') +
                out.headOption.fold(0)(s => s.count(_ == '{'))
            println(nrOfOpenBlock)
            out = out :+ string.substring(start, i + 1) //:+ string.substring(i - 1, i)
            start = i + 1
          case c =>
            ()
        }
        i += 1
      }
      val carry = string.substring(start, string.size)
//      pprint.pprintln(out)
      (out, carry)
    }

    def extractLines(buffer: Vector[String], chunk: Chunk[String], pendingLineFeed: Boolean): (Chunk[String], Vector[String], Boolean) = {
      @annotation.tailrec
      def loop(remainingInput: Vector[String], buffer: Vector[String], output: Vector[String], pendingLineFeed: Boolean): (Chunk[String], Vector[String], Boolean) = {
        if (remainingInput.isEmpty) {
          (Chunk.indexedSeq(output), buffer, pendingLineFeed)
        } else {
          val next = remainingInput.head
          if (pendingLineFeed) {
            if (next.headOption == Some('}')) {
              val out = (buffer.init :+ buffer.last.init).mkString
              loop(next.tail +: remainingInput.tail, Vector.empty, output :+ out, false)
            } else {
              loop(remainingInput, buffer, output, false)
            }
          } else {
            val (out, carry) = linesFromString(next, if(buffer.isEmpty) "" else buffer.last)
            val pendingLF = if (carry.nonEmpty) carry.last == '}' else pendingLineFeed
            loop(remainingInput.tail,
              if (out.isEmpty) buffer :+ carry else Vector(carry),
              if (out.isEmpty) output else output ++ ((buffer :+ out.head).mkString +: out.tail), pendingLF)
          }
        }
      }
      loop(chunk.toVector, buffer, Vector.empty, pendingLineFeed)
    }

    def go(buffer: Vector[String], pendingLineFeed: Boolean, s: Stream[F, String]): Pull[F, String, Option[Unit]] = {
      s.pull.unconsChunk.flatMap {
        case Some((chunk, s)) =>
          val (toOutput, newBuffer, newPendingLineFeed) = extractLines(buffer, chunk, pendingLineFeed)
          Pull.output(toOutput) >> go(newBuffer, newPendingLineFeed, s)
        case None if buffer.nonEmpty => Pull.output1(buffer.mkString) >> Pull.pure(None)
        case None => Pull.pure(None)
      }
    }

    s => go(Vector.empty, false, s).stream
  }

  val buf = new scala.collection.mutable.ListBuffer[String]()

  val css =
    src.through(text.utf8Decode)
      .through(cssBlocks)
      .filter(!_.trim.isEmpty)

  pprint.pprintln(css.runLog.unsafeRunSync())
}