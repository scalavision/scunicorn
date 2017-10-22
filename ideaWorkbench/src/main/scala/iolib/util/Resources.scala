package iolib.util

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object Resources {

  /** helper to create named daemon thread factories **/
  def mkThreadFactory(name: String, daemon: Boolean, exitJvmOnFatalError: Boolean = true): ThreadFactory = {
    new ThreadFactory {
      val idx = new AtomicInteger(0)
      val defaultFactory = Executors.defaultThreadFactory()
      def newThread(r: Runnable): Thread = {
        val t = defaultFactory.newThread(r)
        t.setName(s"$name-${idx.incrementAndGet()}")
        t.setDaemon(daemon)
        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
          def uncaughtException(t: Thread, e: Throwable): Unit = {
            ExecutionContext.defaultReporter(e)
            if (exitJvmOnFatalError) {
              e match {
                case NonFatal(_) => ()
                case fatal => System.exit(-1)
              }
            }
          }
        })
        t
      }
    }
  }

}
