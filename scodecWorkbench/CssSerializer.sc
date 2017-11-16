import $ivy.`org.scodec::scodec-bits:1.1.5`
import $ivy.`org.scodec::scodec-stream:1.1.0-M8`
import $ivy.`co.fs2::fs2-core:0.10.0-M8`
import $ivy.`co.fs2::fs2-io:0.10.0-M8`
//   "com.lihaoyi" %% "ammonite-ops" % "1.0.3",
import scodec.Codec
import scodec.codecs._
import scodec.bits._
import scodec.codecs.implicits._
import fs2._
import cats.effect.IO
import scodec.stream.{encode => E, decode => D, StreamDecoder,StreamEncoder}

  case class StyleSheet(
    id: Int,
    contentHash: Int,
    content: String
    )

  def create(css: String) = {
    StyleSheet(
      css.takeWhile(_ != '{').hashCode(),
      css.hashCode(),
      css
    )
  }

  val stylesheetCodec: Codec[StyleSheet] = {
    int32 :: int32 :: utf8_32
  }.as[StyleSheet]

  val streamDecoder: StreamDecoder[StyleSheet] = D.many(stylesheetCodec)
  val streamEncoder: StreamEncoder[StyleSheet] = E.many(stylesheetCodec)

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

  val cssStream: Stream [IO, StyleSheet] = Stream(css).map(create)

  def logStylesheet(prefix: String): Sink[IO, ByteVector] = _.evalMap { s =>
    IO(pprint.pprintln(s"$prefix > " + s))
  }

  val encodedStream = streamEncoder.encode { cssStream }.map(_.toByteVector).to(logStylesheet("out > "))

  encodedStream.run.unsafeRunSync()

  


