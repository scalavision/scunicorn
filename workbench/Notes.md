# Lift a value into a stream

Use the `emit` function on Stream to create a new Stream
based upon a value from another stream. Do this in conjunction with flatmap :

```scala
import fs2._
Stream(1,2,3).flatMap{n => Stream.emit(List.fill(n)(n)) } 
res4: Stream[Pure, List[Int]] = Stream(..)
```

An effect can be created by using cats-effect and the IO. An example of an effect is:

```scala
val currentTime: IO[Long] = IO { System.currentTimeMillis }
```

To evaluate the effect and turn it into a new Stream, you can use the `eval` function. To convert the Stream into an actual value of the target effect type F, i.e. logging the output values to a Vector, you can use the `runLog` function:

```scala
Stream.eval(currentTime).runLog
```

You have to be careful though, `runLog` on an infinite Stream will exchaust the heap space.

To run everything executing the actual effect we have:

```scala
Stream.eval(currentTime).runLog.unsafeRunAsync(println)
```

`unsafeRunAsync` needs a callback function, here we are just using println to make it simple.

To create a client tcp socket, aligning threadpools etc. you may want to look at the fs2-http `util` package and `Resources.scala` file found in test in the same library.



