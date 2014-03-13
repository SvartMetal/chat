package ru.spbau.network.montsev.chat.protocol.v1

/**
 * Author: Mikhail Montsev
 * Date: 3/12/14
 * Time: 4:11 AM
 **/
object ProtocolV1EventNames {
  val FETCH_REQUEST = "FetchRequest"
  val FETCH_RESPONSE = "FetchResponse"
  val LOGIN_ERROR = "LoginError"
  val LOGIN_REQUEST = "LoginRequest"
  val LOGIN_RESPONSE = "LoginResponse"
  val LOGOUT_REQUEST = "LogoutRequest"
  val LOGOUT_RESPONSE = "LogoutResponse"
  val MESSAGE_LIST_REQUEST = "MessageListRequest"
  val MESSAGE_LIST_RESPONSE = "MessageListResponse"
  val PROCESSING_ERROR = "ProcessingError"
  val SEND_REQUEST = "SendRequest"
  val SEND_RESPONSE = "SendResponse"
  val USER_LIST_REQUEST = "UserListRequest"
  val USER_LIST_RESPONSE = "UserListResponse"
}
