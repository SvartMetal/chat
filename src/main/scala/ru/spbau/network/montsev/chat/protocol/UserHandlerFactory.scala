package ru.spbau.network.montsev.chat.protocol

import ru.spbau.network.montsev.chat.protocol.v1.{ProtocolV1, UserHandlerV1}
import scala.util.Try
import ru.spbau.network.montsev.chat.server.ChatServiceApp

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:46 AM
 **/
object UserHandlerFactory {

  val chatManager = ChatServiceApp.chatManager

  def apply(protocolVersion: String) = Try {
    protocolVersion match {
      case "v1" =>
        UserHandlerV1.props(ProtocolV1, chatManager)
    }
  }

}
