package ru.spbau.network.montsev.chat.protocol

import ru.spbau.network.montsev.chat.protocol.v1.ProtocolV1
import scala.util.Try

/**
 * Author: Mikhail Montsev
 * Date: 3/11/14
 * Time: 1:26 PM
 **/
object ProtocolFactory {

  import Versions._

  def apply(protocolVersion: String) = Try {
    protocolVersion match {
      case PROTOCOL_V1 =>
        ProtocolV1
    }

  }
}
