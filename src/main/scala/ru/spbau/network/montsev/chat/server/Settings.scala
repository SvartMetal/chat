package ru.spbau.network.montsev.chat.server

/**
 * Author: Mikhail Montsev
 * Date: 3/11/14
 * Time: 12:37 AM
 **/
object Settings {
  val MAX_REQUEST_LENGTH = 1024 * 1024
  val HOST = "localhost"
  val PORT = 8080
  val CHAT_SERVICE = "chat-service"
  val PROTOCOL_ACCEPTED = "OK"
  val PROTOCOL_REJECTED = "ERR"
}
