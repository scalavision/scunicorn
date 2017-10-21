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

## Avoid Nothing problem with pure ##

You can avoid Nothing type inference problems by calling pure on the stream:

```
Stream(1,2,3,4,5).pure.through(<my pipe function here>)
```

# Pulling from a Stream

You can pull from a stream like so:
```scala
// Infinite stream lifted into IO effect
val src = Stream(1,2,3).repeat.covary[IO]
// 
src.take(10)
  .pull.uncons.flatMap{ h => 
     Pull.attemptEval( 
       IO{ println("hello") } 
    )
  }.stream.runLog.unsafeRunSync()
```

To print out the internal structure of the stream, you could probably do this:

```scala
src.take(10).pull.uncons.flatMap { h => 
   Pull.attemptEval( IO{ pprint.pprintln(h) } )
}.stream.runLog.unsafeRunSync()

> Some((Chunk(1, 2, 3), Stream(..)))
> res19: Vector[Nothing] = Vector()
```

To manipulate the output of the stream:
```scala
src.take(10).pull.uncons.flatMap{ h => 
  Pull.output1(42)
}.stream.runLog.unsafeRunSync() 

> res20: Vector[Int] = Vector(42)
```
Another one is to write out the head of the Segment:
```scala
src.take(10).pull.uncons.flatMap{ h => 
  pprint.pprintln(h); Pull.output1(h.get._1)
}.stream.runLog.unsafeRunSync() 

> Some((Chunk(1, 2, 3), Stream(..)))
```

# Scheduling the work

To create a Scheduler

```scala
val scheduler = Schedule[IO](corePoolSize = 1)
```

To make it awake every second

```scala
import scala.concurrent.duration._

val awake = scheduler.flatMap { s => s.awakeEvery[IO](1.second) }
```

To run the stream for 5 seconds (it will block)

```scala
awake.take(5).runlog.unsafeRunSync()
```

You can also run it asynchronously

```scala
awake.take(5).runlog.unsafeRunAsync()
```

## Working with interrupts

You can create an eternal stream:
```scala
val ticks = awake.evalMap{ i => IO{println("Time: " + i )}}
```

And the you can interrupt it with `Pipe2` `interrupt` function.

# Logging

You can create a logging function like so:

```scala
def log[A](prefix: String): Pipe[IO, A, A] = 
  _.evalMap{ a => IO { println(s"$prefix> $a"); a }} 
```

To use the log from a pure stream, you have to lift it into the effect using `covary`.

```scala
Stream(1,2,3).covary[IO].through(log("")).run 

// gives the result of:
res13: IO[Unit] = BindSuspend(
  Single(cats.effect.IO$$$Lambda$1939/883455411@93a6a7d),
  Single(cats.effect.IO$$Lambda$2235/2054611385@9a9eecc)
)
// We can run it like so:
@ res13.unsafeRunSync() 
> 1
> 2
> 3
```

# Random delays

Trying out random delays:

```scala
def randomDelays[A](max: FiniteDuration): Pipe[IO,A,A] = _.evalMap { a => 
  val delay = IO { scala.util.Random.nextInt(max.toMillis.toInt )}
  delay.flatMap { d => IO { IO.pure(a).unsafeRunTimed(d.millis).get }}
  } 
```
A wrongly attempt on using delays:

```scala
scheduler.flatMap{ s => 
  Stream.range(1,10).covary[IO].through(s.debounce(1.second)).through(log("A")) 
}.run.unsafeRunSync()
```

# Signals

You can create a signal like this:

```scala
val x = async.signalOf[IO, Int](1)
```

To extract the signal value, you can run it with flatMap:
```scala
import fs2.async.immutable.Signal

x.flatMap { x1: Signal[IO, Int] = x1.get }.unsafeRunSync()
```

To create a Stream from the signal you can use `Stream.eval`:

```scala
Stream.eval(x).flatMap { x => x.discrete }
```

More advanced Signal example:

```scala
Stream.eval(async.signalOf[IO, Int](0)).flatMap { s =>
    // monitor writes output of discrete method to our log function
    // we drain the stream after that because we don't need the values anymore
    // we also set that the stream should return nothing, as we don't need the data
    val monitor: Stream[IO, Nothing] = s.discrete.through(log("s updated")).drain
    // This is just generating data, ideally we would put a randomDelay here to make
    // the example more realistic, but don't know how to do that as of yet ..
    val data = Stream.range(10, 20)
    // We create a writer for our data, so we set the value of Signal to be
    // the output of the Stream (i.e. from 10 to 20)
    val writer: Stream[IO, Unit] = data.evalMap { d => s.set(d) }
    // We merge the monitor with the writer to make them run in parallell
    // We are probably not guaranteed to see all outputs in the log, because of
    // the nature of merge and log, using queues we do have this guarantee.
    // See below
    monitor merge writer
  } 
```
This example will run through the Stream, then `hang` on the output, becaus the monitor process will run eternally. 

To fix this we use `mergeHaltBoth`:

```scala
 monitor mergeHaltBoth writer
 ```

# Queues

You can also use queues instead of Signal.

```scala
Stream.eval(async.boundedQueue[IO, Int](5)).flatMap { q =>
    val monitor: Stream[IO, Nothing] = q.dequeue.through(log("s updated")).drain
    // Needed to lift it up into an effect, didn't need to do that on Signal ???
    val data = Stream.range(10, 20).covary[IO]
    // `d.enqueue` is a Sink, so we can use the `to` method on the stream
    val writer: Stream[IO, Unit] = data.to(q.enqueue)
    // Because we are using queues we are guaranteed that we will see
    // all values written to the log ...
    monitor mergeHaltBoth writer
  }.run.unsafeRunSync() 

// Gives the following output:
s updated> 10
s updated> 11
s updated> 12
s updated> 13
s updated> 14
s updated> 15
s updated> 16
s updated> 17
s updated> 18
```

We have not been looking into :

* async.semaphore - much like semaphores in Java
* async.topic: Publishers and Consumers with backpressure

























