package ru.spbau.network.montsev.chat.server

import akka.actor._
import akka.io.Tcp
import spray.http.HttpMethods._
import ru.spbau.network.montsev.chat.protocol.UserHandlerFactory
import spray.http.HttpRequest
import spray.http.HttpResponse
import scala.util.Success
import akka.actor.Terminated

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:41 AM
 **/
object ConnectionHandler {
  def props(connection: ActorRef): Props = Props(new ConnectionHandler(connection))
}

class ConnectionHandler(connection: ActorRef) extends Actor with ActorLogging {
  import Settings._

  context.watch(connection)

  private def debug(text: String) {
    log.debug(s"[ConnectionHandler] $text")
  }

  private def watching: Receive = {
    case _: Tcp.ConnectionClosed =>
      context.stop(self)

    case Terminated(`connection`) =>
      context.stop(self)
  }

  private def waitingForRequest(userHandler: ActorRef): Receive = {
    case HttpRequest(POST, _, _, entity, _) =>
      val remote = sender()
      if (entity.data.length <= Settings.MAX_REQUEST_LENGTH) {
        val plainMsg = entity.asString
        debug(s"{waitingForRequest} Working with request: $plainMsg")

        userHandler ! plainMsg
        debug("{waitingForRequest} Request sent to UserHandler. Become waitingForResponse")
        context.become(waitingForResponse(remote, userHandler))
      }
  }

  private def waitingForResponse(remote: ActorRef, userHandler: ActorRef): Receive =  {
    case response: String =>
      debug(s"{waitingForResponse} Response from UserHandler received: $response")
      debug("{waitingForResponse} Sending response to client. Become waitingForRequest")
      remote ! HttpResponse(entity = response)
      context.become(waitingForRequest(userHandler))

  }

  override def receive: Receive = beforeLogin orElse watching

  private def beforeLogin: Receive = {
    case HttpRequest(POST, _, _, entity, _) =>
      if (entity.data.length <= Settings.MAX_REQUEST_LENGTH) {
        debug("{beforeLogin} Protocol version received")
        val remote = sender()
        val plainMsg = entity.asString
        UserHandlerFactory(plainMsg) match {
          case Success(userHandlerProps) =>
            val userHandler = context.actorOf(userHandlerProps)

            debug("{beforeLogin} Become waitingForRequest")
            remote ! HttpResponse(entity = PROTOCOL_ACCEPTED)
            context.become(waitingForRequest(userHandler) orElse watching)

          case _ =>
            debug("{beforeLogin} Protocol version is invalid. Sending response to client")
            remote ! HttpResponse(entity = PROTOCOL_REJECTED)
        }

      }
  }
}
