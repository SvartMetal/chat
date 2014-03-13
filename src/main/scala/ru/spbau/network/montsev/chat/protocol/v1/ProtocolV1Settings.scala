package ru.spbau.network.montsev.chat.protocol.v1

/**
 * Author: Mikhail Montsev
 * Date: 3/11/14
 * Time: 1:27 PM
 **/
object ProtocolV1Settings {
  def validateUserName(name: String) = name.trim() != INVALID_USER_NAME

  val INVALID_USER_NAME = ""
}
