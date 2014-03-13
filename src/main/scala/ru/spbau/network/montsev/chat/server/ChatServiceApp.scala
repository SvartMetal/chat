package ru.spbau.network.montsev.chat.server

import akka.actor.ActorSystem

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:43 AM
 **/
object ChatServiceApp extends App {
  import Settings._

  val system = ActorSystem(CHAT_SERVICE)

  system.actorOf(ChatService.props(HOST, PORT))

  val chatManager = system.actorOf(ChatManager.props())

  readLine("Hit ENTER to shutdown...\n")

  system.shutdown()
  system.awaitTermination()
}
