package ru.spbau.network.montsev.chat.protocol.v1

import ru.spbau.network.montsev.chat.protocol.{Protocol, Event}
import scala.util.Try
import scala.xml.{Node, XML}
import scala.xml.Utility.trim

import ProtocolV1EventNames._
import ru.spbau.network.montsev.chat.server.Message

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 6:06 AM
 **/
object ProtocolV1 extends Protocol {

  override def apply(event: Event): String = event.toString

  override def apply(msg: String): Try[Event] = Try {
    val xml = XML.loadString(msg)
    trim(xml) match {
      case <xml><head>{request}</head>{content}</xml> =>

        request.text match {
          case LOGIN_REQUEST =>
            LoginRequest.fromXMLContentTag(content)

          case LOGIN_RESPONSE =>
            LoginResponse.fromXMLContentTag(content)

          case FETCH_REQUEST =>
            FetchRequest.fromXMLContentTag(content)

          case FETCH_RESPONSE =>
            FetchResponse.fromXMLContentTag(content)

          case LOGIN_ERROR =>
            LoginError.fromXMLContentTag(content)

          case SEND_REQUEST =>
            SendRequest.fromXMLContentTag(content)

          case SEND_RESPONSE =>
            SendResponse.fromXMLContentTag(content)

          case MESSAGE_LIST_REQUEST =>
            MessageListRequest.fromXMLContentTag(content)

          case MESSAGE_LIST_RESPONSE =>
            MessageListResponse.fromXMLContentTag(content)

          case USER_LIST_REQUEST =>
            UserListRequest.fromXMLContentTag(content)

          case USER_LIST_RESPONSE =>
            UserListResponse.fromXMLContentTag(content)

          case LOGOUT_REQUEST =>
            LogoutRequest.fromXMLContentTag(content)

          case LOGOUT_RESPONSE =>
            LogoutResponse.fromXMLContentTag(content)

          case PROCESSING_ERROR =>
            ProcessingError.fromXMLContentTag(content)

        }
    }
  }
}

abstract class BaseEventObject(eventName: String) {
  def fromString(data: String) = XML.loadString(data) match {
    case <xml><head>{name}</head>{content}</xml> if name.text == eventName =>
      fromXMLContentTag(content)
  }
  
  def fromXMLContentTag(xml: Node): Event
}

object LoginRequest extends BaseEventObject("LoginRequest") {
  
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><name>{user}</name></content> =>
      LoginRequest(user.text)
  }
}

case class LoginRequest(name: String) extends Event {

  override def toString = {
    <xml>
      <head>{LOGIN_REQUEST}</head>
      <content>
        <name>{name}</name>
      </content>
    </xml>
  }.toString()
}

object LoginResponse extends BaseEventObject("LoginResponse") {
  
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><id>{id}</id></content> =>
      LoginResponse(id.text.toLong)
  }
}

case class LoginResponse(lastMessageId: Long) extends Event {
  override def toString = {
    <xml>
      <head>{LOGIN_RESPONSE}</head>
      <content>
        <id>{lastMessageId}</id>
      </content>
    </xml>
  }.toString()
}

object LoginError extends BaseEventObject("LoginError") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><name>{user}</name><info>{errorInfo}</info></content> =>
      LoginError(user.text, errorInfo.text)
  }
}

case class LoginError(name: String, info: String) extends Event {
  override def toString = {
    <xml>
      <head>{LOGIN_ERROR}</head>
      <content>
        <name>{name}</name>
        <info>{info}</info>
      </content>
    </xml>
  }.toString()
}

object FetchRequest extends BaseEventObject("FetchRequest") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content></content> =>
      FetchRequest()
  }
}

case class FetchRequest() extends Event {
  override def toString = {
    <xml>
      <head>{FETCH_REQUEST}</head>
      <content>
      </content>
    </xml>
  }.toString()
}

object FetchResponse extends BaseEventObject("FetchResponse") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content>{lastMessageId}</content> =>
      FetchResponse(lastMessageId.text.toLong)
  }
}

case class FetchResponse(lastMessageId: Long) extends Event {
  override def toString = {
    <xml>
      <head>{FETCH_RESPONSE}</head>
      <content>{lastMessageId}</content>
    </xml>
  }.toString()
}

object SendRequest extends BaseEventObject("SendRequest") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><id>{clientMessageId}</id><data>{msg}</data></content> =>
      SendRequest(clientMessageId.text.toLong, msg.text)
  }
}

case class SendRequest(clientMessageId: Long, data: String) extends Event {
  override def toString = {
    <xml>
      <head>{SEND_REQUEST}</head>
      <content>
        <id>{clientMessageId}</id>
        <data>{data}</data>
      </content>
    </xml>
  }.toString()
}

object SendResponse extends BaseEventObject("SendResponse") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><clid>{clientMessageId}</clid><id>{messageId}</id><time>{timeStamp}</time></content> =>
      SendResponse(clientMessageId.text.toLong, timeStamp.text, messageId.text.toLong)
  }
}

case class SendResponse(clientMessageId: Long, timeStamp: String, messageId: Long) extends Event {
  override def toString = {
    <xml>
      <head>{SEND_RESPONSE}</head>
      <content>
        <clid>{clientMessageId}</clid>
        <id>{messageId}</id>
        <time>{timeStamp}</time>
      </content>
    </xml>
  }.toString()
}

object MessageListRequest extends BaseEventObject("MessageListRequest") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><lower>{lowerId}</lower><upper>{upperId}</upper></content> =>
      MessageListRequest(lowerId.toString().toLong, upperId.toString().toLong)
  }
}

case class MessageListRequest(lowerId: Long, upperId: Long) extends Event {
  override def toString = {
    <xml>
      <head>{MESSAGE_LIST_REQUEST}</head>
      <content>
        <lower>{lowerId}</lower>
        <upper>{upperId}</upper>
      </content>
    </xml>
  }.toString()
}

object MessageListResponse extends BaseEventObject("MessageListResponse") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><lower>{lowerId}</lower><upper>{upperId}</upper>{messages}</content> =>
      MessageListResponse(lowerId.text.toLong, upperId.text.toLong, (messages \ "msg").map(Message.fromXML).toList)
  }
}

case class MessageListResponse(lowerId: Long, upperId: Long, messages: List[Message]) extends Event {
  override def toString = {
    <xml>
      <head>{MESSAGE_LIST_RESPONSE}</head>
      <content>
        <lower>{lowerId}</lower>
        <upper>{upperId}</upper>
        <data>{messages.map(_.toXML).flatten}</data>
      </content>
    </xml>
  }.toString()
}

object UserListRequest extends BaseEventObject("UserListRequest") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content></content> =>
      UserListRequest()
  }
}

case class UserListRequest() extends Event {
  override def toString = {
    <xml>
      <head>{USER_LIST_REQUEST}</head>
      <content>
      </content>
    </xml>
  }.toString()
}

object UserListResponse extends BaseEventObject("UserListResponse") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content>{users}</content> =>
      UserListResponse((users \ "user").map(_.text).toList)
  }
}

case class UserListResponse(users: List[String]) extends Event {
  override def toString = {
    <xml>
      <head>{USER_LIST_RESPONSE}</head>
      <content>
        <data>{users.map(p => <user>{p}</user>).flatten}</data>
      </content>
    </xml>
  }.toString()
}

object LogoutRequest extends BaseEventObject("LogoutRequest") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><name>{user}</name></content> =>
      LogoutRequest(user.text)
  }
}

case class LogoutRequest(user: String) extends Event {
  override def toString = {
    <xml>
      <head>{LOGOUT_REQUEST}</head>
      <content>
        <name>{user}</name>
      </content>
    </xml>
  }.toString()
}

object LogoutResponse extends BaseEventObject("LogoutResponse") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><info>{info}</info></content> =>
      LogoutResponse(info.text)
  }
}

case class LogoutResponse(info: String) extends Event {
  override def toString = {
    <xml>
      <head>{LOGOUT_RESPONSE}</head>
      <content>
        <info>{info}</info>
      </content>
    </xml>
  }.toString()
}

object ProcessingError extends BaseEventObject("ProcessingError") {
  def fromXMLContentTag(xml: Node): Event = xml match {
    case <content><info>{info}</info></content> =>
      ProcessingError(info.text)
  }
}

case class ProcessingError(info: String) extends Event {
  override def toString = {
    <xml>
      <head>{PROCESSING_ERROR}</head>
      <content>
        <info>{info}</info>
      </content>
    </xml>
  }.toString()
}

