package ru.spbau.network.montsev.chat.protocol.v1

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import akka.pattern.ask
import ru.spbau.network.montsev.chat.protocol.{Event, Protocol}
import scala.util.Success
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import ru.spbau.network.montsev.chat.server.{ChatStorage, ChatManager}

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:44 AM
 **/
object UserHandlerV1 {
  def props(protocol: Protocol, chatManager: ActorRef): Props = Props(new UserHandlerV1(protocol, chatManager))
}

class UserHandlerV1(protocol: Protocol, chatManager: ActorRef) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  import ProtocolV1Settings._

  implicit val timeout = Timeout(5.seconds)

  private var myUserName = INVALID_USER_NAME

  override def postStop() {
    if (validateUserName(myUserName)) {
      debug("{postStop} Sending Logout to ChatManager")
      chatManager ! ChatManager.Logout(myUserName)
    }
  }

  private def debug(text: String) {
    log.debug(s"[UserHandler] $text")
  }

  private def sendResponse(connection: ActorRef, event: Event) {
    connection ! protocol(event)
  }

  private def waitingForLogin(connection: ActorRef, userName: String): Receive = {
    case ChatManager.LoginError(info) =>
      debug(s"{waitingForLogin} LoginError: $info")
      sendResponse(connection, LoginError(name = userName, info = info))
      context.become(beforeLogin)

    case ChatManager.LoginOk(_) =>
      debug(s"{waitingForLogin} Login OK. Become working")
      sendResponse(connection, LoginResponse(ChatStorage.lastId))
      myUserName = userName
      context.become(working(connection))
  }

  private def waitingForLogout(connection: ActorRef): Receive = {
    case ChatManager.LogoutError(info) =>
      debug(s"{waitingForLogout} LogoutError: $info. Sending ProcessingError to client and stopping")
      sendResponse(connection, ProcessingError("Logout error. Something strange happened. "))
      myUserName = INVALID_USER_NAME
      context.stop(self)

    case ChatManager.LogoutOk(_) =>
      debug(s"{waitingForLogout} Logout OK. Stopping")
      sendResponse(connection, LogoutResponse(s"Logout of user: $myUserName complete. "))
      myUserName = INVALID_USER_NAME
      context.stop(self)
  }

  private def working(connection: ActorRef): Receive = {
    case plainMsg: String =>
      debug(s"{working} Text received: $plainMsg")
      protocol(plainMsg) match {
        case Success(smth) =>
          smth match {
            case FetchRequest() =>
              sendResponse(connection, FetchResponse(ChatStorage.lastId))

            case SendRequest(clientMessageId, text) =>
              val msg = ChatStorage.storeMessage(user = myUserName, data = text)
              sendResponse(connection, SendResponse(clientMessageId = clientMessageId,
                timeStamp = msg.timeStamp, messageId = msg.id))

            case UserListRequest() =>
              val future: Future[ChatManager.UserListResponse] =
                ask(chatManager, ChatManager.UserListRequest()).mapTo[ChatManager.UserListResponse]

              future.onSuccess {
                case ChatManager.UserListResponse(users) =>
                  sendResponse(connection, UserListResponse(users = users))
              }

            case MessageListRequest(lowerId, upperId) =>
              val msgList = ChatStorage.messageList(lowerId, upperId)
              sendResponse(connection, MessageListResponse(lowerId = lowerId, upperId = upperId, messages = msgList))

            case LogoutRequest(_) =>
              chatManager ! ChatManager.Logout(myUserName)
              context.become(waitingForLogout(connection))

            case _ =>
              debug(s"{working} LoginRequest now is forbidden. Sending ProcessingError to client")
              sendResponse(connection, ProcessingError("LoginRequest is not permitted. "))
          }

        case _ =>
          debug("{working} Unknown request. Failed to parse")
          sendResponse(connection, ProcessingError("Bad request. "))
      }
  }

  private def beforeLogin: Receive = {
    case plainMsg: String =>
      debug(s"{beforeLogin} Plain text received: $plainMsg")
      val connection = sender()
      protocol(plainMsg) match {
        case Success(LoginRequest(userName)) =>
          debug(s"{beforeLogin} Protocol passed. User name is: $userName")
          if (validateUserName(userName)) {
            chatManager ! ChatManager.Login(userName)

            debug("{beforeLogin} Trying to login and become working")
            context.become(waitingForLogin(connection, userName))
          } else {
            debug("{beforeLogin} Protocol parsing failed. Sending LoginError to client")
            sendResponse(connection, LoginError(name = userName, info = "User name is invalid. "))
          }

        case _ =>
          debug("{beforeLogin} Requests except LoginRequest are forbidden. Sending LoginError to client")
          sendResponse(connection, LoginError(name = "Undefined", info = "You must login before sending messages. "))
      }

  }

  override def receive: Receive = beforeLogin
}
