package cssprocessor

  import fs2._
  import cssprocessor.CssSerializer.StyleSheet
  import org.specs2.mutable.Specification
  import scodec.Codec
  import scodec.codecs._
  import scodec.bits._
  import scodec.codecs.implicits._
  import Resources._
  import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.IO
import org.scalacheck._
import Prop._
import fs2.Stream
import scodec.bits.BitVector
import scodec.{ Attempt, Decoder, Err }
import scodec.codecs._
import scodec.stream.{decode => D, encode => E}

class SerializerSpec extends Specification {

  "serializer" should {

    "should be able to use scodec" in {

      val css =
        """
          |a {
          |  display: flex;
          |}
          |
          |h1, h2, h3, h4 {
          |  font-size: 12px;
          |}
        """.stripMargin

      val cssStream: Stream[IO, StyleSheet] = Stream(css).map(StyleSheet.create).covary[IO]

      def logStylesheet(prefix: String): Sink[IO, BitVector] = _.evalMap { s =>
        IO(println(s"$prefix > " + s))
      }

      val encodedStream: Stream[IO, ByteVector] =
        CssEncoder.streamEncoder.encode { cssStream }.map(_.toByteVector)

          //.covary[IO].to(logStylesheet("out > "))


//      val s = StyleSheet.enc(css)

      //pprint.pprintln(s)



      1 === 1

    }
  }

}
