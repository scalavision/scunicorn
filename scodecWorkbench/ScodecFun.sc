import $ivy.`org.scodec::scodec-bits:1.1.5`
import $ivy.`org.scodec::scodec-stream:1.0.1`
import $ivy.`co.fs2::fs2-core:0.10.0-M8`
import $ivy.`co.fs2::fs2-io:0.10.0-M8`

import scodec.Codec
import scodec.codecs._
import scodec.bits._
import scodec.codecs.implicits._

// ByteVector is a vector of bytes
val x: ByteVector = hex"deadbeef"

pprint.pprintln(x)
println(x)

val fH = ByteVector.fromHex("deadbeef")
println(fH)

// BitVector works as a ByteVector with the difference that
// you can operate on the bit level instead of at the byte level
val b: BitVector = bin"10"
println(b)
val c: BitVector = b.update(1, true)
println(c)

case class Point(x: Int, y: Int)
case class Line(start: Point, end: Point)
case class Arrangement(lines: Vector[Line])

val arr = Arrangement(Vector(
  Line(Point(0,0), Point(1,0)),
  Line(Point(0,0), Point(0,1)),
  Line(Point(0,0), Point(1,1))
))

val arrBinary = Codec.encode(arr).require
//println(arrBinary)

val decoded = Codec[Arrangement].decode(arrBinary).require.value
//pprint.pprintln(arrBinary)

// importing other kind of implicit into scope
/*
import scodec.codecs.implicits.{ implicitIntCodec => _, _ }
implicit val ci = scodec.codecs.uint8
println("As Int's instead")

val arrBinaryOfIntValues = Codec.encode(arr).require
println(arrBinary)

val decodedOfIntValue = Codec[Arrangement].decode(arrBinaryOfIntValues).require.value
pprint.pprintln(arrBinary)
*/

//import scodec.codecs.implicits.{ implicitIntCodec => _, _ }

import scodec.Decoder
//import scodec.codecs.ImplicitValues._
import scodec.codecs.implicits._

case class Foo(n: Int)

val fooDecoder: Decoder[Foo] = int32 map { i => Foo(i) }
pprint.pprintln(fooDecoder)

// To Decode a Foo, we need a BitVector of 32 bits, i.e. to be able to 
// create an Int. Here we have value 1
fooDecoder.decode(bin"00000000000000000000000000000001") 

// Working with simple encode /decode of primitives
val intCodec = uint16L
val intAsBitVector = intCodec.encode(10)
// Decodeing it with our intCodec
val ourInt = intCodec.decode(intAsBitVector.require) 

// Correctly encode and decoding string. You need to choose a structure
// that preserves the size of the string. In this example the size will
// be preserved in a 32 bit field preceeding the encoded string content.
// Instead of using a hardcoded length of 32 bit, you can use other sizes
// with the variableSizeBytes function
val pair = utf8_32 ~ uint8 
val encodedPair = pair.encode(("Hello", 42))

val decodePair = pair.decode(encodedPair.require)
// to extract the value
val decodedPairValue = decodePair.require.value


case class CssBlock(id: Int, length: Int, css: String)

val css = """
  p > a {
    border: solid red 2px;
    color: red;
  }
  """




  


