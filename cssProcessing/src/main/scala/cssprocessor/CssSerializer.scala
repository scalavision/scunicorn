package cssprocessor

import scodec.{Attempt, Codec, DecodeResult}
import scodec.codecs._
import scodec.bits._
import scodec.codecs.implicits._
import scodec.stream.{encode,decode,StreamDecoder,StreamEncoder}


object CssSerializer {

  case class StyleSheet(
    id: Int,
    contentHash: Int,
    content: String
  )

  object StyleSheet {

    def create(css: String) = {
      StyleSheet(
        css.takeWhile(_ != '{').hashCode(),
        css.hashCode(),
        css
      )
    }

    def enc(css: String): Attempt[BitVector] =
      stylesheetCodec.encode(create(css))

    def dec(bitVector: BitVector): Attempt[DecodeResult[StyleSheet]] =
      stylesheetCodec.decode(bitVector)

  }

  val stylesheetCodec: Codec[StyleSheet] = {
    int32 :: int32 :: utf8_32
  }.as[StyleSheet]

}

object CssDecoder {
  import CssSerializer._
  val streamDecoder: StreamDecoder[StyleSheet] = decode.many(stylesheetCodec)
}

object CssEncoder {
  import CssSerializer._
  val streamEncoder: StreamEncoder[StyleSheet] = encode.many(stylesheetCodec)
}
