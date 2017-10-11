
lazy val fs2Version = "0.10.0-M6"

import $ivy.`co.fs2::fs2-core:0.10.0-M6`
import $ivy.`co.fs2::fs2-io:0.10.0-M6`

import cats.effect.{IO, Effect, Sync, Async}
import fs2._

val currentTime: IO[Long] = IO { System.currentTimeMillis }

Stream.eval(currentTime).runLog.unsafeRunAsync(println)
