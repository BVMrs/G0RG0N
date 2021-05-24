package com.bvmrs.bckch

import cats.syntax.all._
import cats.effect.std.Console
import cats.effect.{Concurrent, ExitCode, Fiber, IO, IOApp, MonadCancelThrow}
import com.bvmrs.bckch.domain.ProtocolMessage
import com.comcast.ip4s.{IpLiteralSyntax, SocketAddress}
import fs2.{Chunk, Stream, text}
import fs2.io.net.Network
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.config.parser
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

import java.io.InvalidObjectException

object Main extends IOApp{
  // It would be for the best to perform the separations at a bytestream level.
  // Given that these messages will carry an arbitrarily long payload, they will
  // probably not only consist only a single standard TCP/UDP packet

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- nodeFrontEnd[IO]
    } yield ExitCode.Success

  def giveUuid = java.util.UUID.randomUUID.toString

  val neighbors = Map.empty[String, String]

  /**
   * Essentially: read->do something (actual node logic in the context of forming the p2p network)->write response
   * Managing the network needs to be handled differently than the actual business logic.
   * Protocol needs to have signalling messages + business messages.
   *
   * Communication goes by TCP so we don't actually need JSON or XML for this part. A protocol + messages for this kind
   * of communication is sufficient.
   *
   * Protocol might be text based:
   *  - Message types:
   *    - Join;
   *    - Voluntary Leave
   *
   * Given the fact that a node may unexpectedly quit the network at any given time, we need a way to identify the
   * disappearance of the give node => we can preemptively remove that 
   *
   * @tparam F
   * @return
   */
  def nodeFrontEnd[F[_]: Concurrent: Network]: F[Unit] =
    Network[F].server(port = Some(port"5555")).map { client =>
      client.reads
        .through(text.utf8Decode)
        .flatMap(decoder[ProtocolMessage])
//        .interleave(Stream.constant("\n"))
        .through(text.utf8Encode)
        .through(client.writes)
        .handleErrorWith(_ => Stream.empty) // handle errors of client sockets
    }.parJoin(100).compile.drain

  /**
   * Decode protocol message.
   *
   * Maybe do this on bytes instead of text in the future.
   * Design considerations:
   *  - fixed structure? =>
   *      PROS: easier decoding
   *      CONS: less tolerant to structure changes, more memory efficient for metadata
   *  - dynamic structure (named fields) =>
   *      PROS: tolerant to protocol evolutions
   *      CONS: much more redundant data (field names)
   *
   * For the purpose of the overlay network, it is preferable to keep metadata exchange at a minimum.
   *
   * Protocol fields - draft 1:
   *  - version
   *  - sourceId
   *  - destinationId
   *  - errCode
   *  - msgType
   *  - crc
   *  - body
   *
   *  Ids shall consist of NAME + IP + PORT
   *  msgType - Broadcast // communication // graceful disconnect
   * @param incomingText
   * @return
   */
  def decoder[T <: ProtocolMessage](incomingText: String)(implicit decoder: Decoder[T]) = {
    Stream.emit(incomingText)
      .flatMap(msg => Stream.emit(parse(msg).flatMap(_.as[T])))
//      .flatMap {
//        case Left(parsingFailure) => Stream.raiseError(new InvalidObjectException(s"Parsing Failed + ${parsingFailure}"))
//        case Right(value) => Stream.emit(value.as[T])
//      }
  }

  //TODO: Protocol message
  //case class ProtocolMessage(etc etc etc)

  def client[F[_]: MonadCancelThrow: Console: Network]: F[Unit] =
    Network[F].client(SocketAddress(host"localhost", port"5555")).use { socket =>
      socket.write(Chunk.array("Hello, world!".getBytes)) >>
        socket.read(8192).flatMap { response =>
          Console[F].println(s"Response: $response")
        }
    }
}
