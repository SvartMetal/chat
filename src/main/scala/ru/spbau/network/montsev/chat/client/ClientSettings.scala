package ru.spbau.network.montsev.chat.client

import scala.concurrent.duration._

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:02 AM
 **/
object ClientSettings {
  val CLIENT = "chat-client"
  val PROTOCOL_VERSION = "v1"
  val RECEIVE_TIMEOUT = 100.milliseconds
  val SHUTTING_DOWN_TIMEOUT = 5.seconds
  val WAITING_FOR_MESSAGES_TIMEOUT = 4.seconds
  val PROTOCOL_ACCEPTED = "OK"
}
