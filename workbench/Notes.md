# Lift a value into a stream

Use the `emit` function on Stream to create a new Stream
based upon a value. You can combine this with flatmap :

```scala
import fs2._
Stream(1,2,3).flatMap{n => Stream.emit(List.fill(n)(n)) } 
res4: Stream[Pure, List[Int]] = Stream(..)
```

An effect can be created by using cats-effect and the IO. An example of an effect is:

```scala
val currentTime: IO[Long] = IO { System.currentTimeMillis }
```

To evaluate the effect and turn it into a Stream, you can use the `eval` function. To convert the Stream into an actual value, you can use the `runLog` function:

```scala
Stream.eval(currentTime).runLog
```

To run everything executing the actual effect we hav:

```scala
Stream.eval(currentTime).runLog.unsafeRunAsync(println)
```

`unsafeRunAsync` needs a callback function, here we are just using prinln to make it simple.


