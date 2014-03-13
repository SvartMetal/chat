package ru.spbau.network.montsev.chat.server

import akka.actor.{ActorLogging, Actor, Props}
import akka.io.IO
import spray.can.Http

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:00 AM
 **/
object ChatService {
  def props(host: String, port: Int) = Props(new ChatService(host, port))
}

class ChatService(host: String, port: Int) extends Actor with ActorLogging {
  import context.system

  log.debug("[ChatService] Binding http spot")
  IO(Http) ! Http.Bind(self, host, port)

  override def receive: Receive = {
    case Http.Connected(_, _) =>
      log.debug("[ChatService] {receive} Someone is connecting. Registering handler")
      sender ! Http.Register(context.actorOf(ConnectionHandler.props(sender())))
  }
}