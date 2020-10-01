/*
 * Copyright 2013-2020 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s
package server

import cats.implicits._
import cats.effect._
import cats.effect.concurrent.Ref
import fs2._
import fs2.concurrent.{Signal, SignallingRef}
import java.net.InetSocketAddress
import scala.collection.immutable

trait ServerBuildable[F[_], A] {

  protected implicit def F: Concurrent[F]

  final def bindHttp(a: A)(port: Int = defaults.HttpPort, host: String = defaults.Host): A =
    bindSocketAddress(a)(InetSocketAddress.createUnresolved(host, port))

  final def bindLocal(a: A)(port: Int): A = bindHttp(a)(port, defaults.Host)

  final def bindAny(a: A)(host: String = defaults.Host): A = bindHttp(a)(0, host)

  /**
    * Runs the server as a process that never emits.  Useful for a server
    * that runs for the rest of the JVM's life.
    */
  final def serve(a: A): Stream[F, ExitCode] =
    for {
      signal <- Stream.eval(SignallingRef[F, Boolean](false))
      exitCode <- Stream.eval(Ref[F].of(ExitCode.Success))
      serve <- serveWhile(a)(signal, exitCode)
    } yield serve

  /**
    * Runs the server as a Stream that emits only when the terminated signal becomes true.
    * Useful for servers with associated lifetime behaviors.
    */
  final def serveWhile(a: A)(
      terminateWhenTrue: Signal[F, Boolean],
      exitWith: Ref[F, ExitCode]): Stream[F, ExitCode] =
    Stream.resource(resource(a)) *> (terminateWhenTrue.discrete
      .takeWhile(_ === false)
      .drain ++ Stream.eval(exitWith.get))

  /** Disable the banner when the server starts up */
  final def withoutBanner(a: A): A = withBanner(a)(immutable.Seq.empty)

  // Abstract Methods //

  def bindSocketAddress(a: A)(socketAddress: InetSocketAddress): A

  def withServiceErrorHandler(a: A)(
      serviceErrorHandler: Request[F] => PartialFunction[Throwable, F[Response[F]]]): A


  /** Returns a Server resource.  The resource is not acquired until the
    * server is started and ready to accept requests.
    */
  def resource(a: A): Resource[F, Server]

  /** Set the banner to display when the server starts up */
  def withBanner(a: A)(banner: immutable.Seq[String]): A
}
