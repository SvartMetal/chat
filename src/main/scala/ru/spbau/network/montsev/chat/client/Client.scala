package ru.spbau.network.montsev.chat.client

import akka.actor._
import ru.spbau.network.montsev.chat.protocol.{Event, Protocol}
import scala.collection.mutable.ArrayBuffer
import spray.http.HttpEntity
import spray.http.HttpMethods._
import spray.can.Http
import akka.io.{Tcp, IO}
import spray.http.HttpRequest
import scala.util.Success
import spray.http.HttpResponse
import ru.spbau.network.montsev.chat.protocol.v1._
import ru.spbau.network.montsev.chat.server.Message

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:02 AM
 **/
object Client {

  case class SendMessage(data: String)
  case class WaitingForMessagesExpired()
  case class Stop()

  def props(userName: String, protocolVersion: String, host: String, port: Int, protocol: Protocol) =
    Props(new Client(userName, protocolVersion, host, port, protocol))
}

class Client(var userName: String, protocolVersion: String, host: String, port: Int, protocol: Protocol)
  extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global
  import context.system
  import Client._
  import ClientSettings._
  import Printer._
  import Commands._

  private var myLastId: Long = -1
  private var myLastPrintedId: Long = -1
  private var myWaitingForMessages = false
  private var myWaitingTask: Cancellable = null
  private val myMessagesSent = ArrayBuffer[String]()

  private def debug(text: String) {
    log.debug(s"[Client] $text")
  }

  private def sendRequest(connection: ActorRef, entity: String) {
    connection ! HttpRequest(method = POST, entity = entity)
  }

  private def sendRequest(connection: ActorRef, event: Event) {
    sendRequest(connection, protocol(event))
  }

  private def receiveMessage(entity: HttpEntity)(code: (Event => Unit)) {
    val plainMsg = entity.asString
    protocol(plainMsg) match {
      case Success(smth) =>
        code(smth)

      case _ =>
        printlnErr(s"Something bad happened. Match failed. Response from server: $plainMsg")
    }
  }

  private def printAlignedMessages() {
    val (printedId, messages) = LocalStorage.allMessagesAlignedFromId(myLastPrintedId, myLastId)
    myLastPrintedId = printedId
    messages.foreach(printlnMsg)
  }

  private def setLastId(id: Long) {
    if (id > myLastId) {
      myLastId = id
    }
  }

  context.setReceiveTimeout(RECEIVE_TIMEOUT)

  IO(Http) ! Http.Connect(host, port)

  private def watching(connection: ActorRef): Receive = {
    case _: Tcp.ConnectionClosed =>
      context.stop(self)

    case Terminated(`connection`) =>
      context.stop(self)

    case Stop() =>
      context.stop(connection)
      context.stop(self)
  }

  private def waitingForProtocolAccepted(connection: ActorRef): Receive = {
    case HttpResponse(_, entity, _, _) =>
      entity.data.asString match {
        case PROTOCOL_ACCEPTED =>
          debug("{waitingForProtocolAccepted} Protocol accepted. Sending LoginRequest and become waitingForLogin")
          sendRequest(connection, LoginRequest(userName))
          context.become(waitingForLogin(connection) orElse watching(connection))

        case _ =>
          debug("{waitingForProtocolAccepted} Protocol is invalid. Stopping")
          printlnErr("Protocol is invalid. Stopping")
          context.stop(self)
      }

  }

  private def waitingForLogin(connection: ActorRef): Receive = {
    case HttpResponse(_, entity, _, _) =>
      debug(s"{waitingForLogin} Http response received: $entity")
      receiveMessage(entity) {
        case LoginResponse(id) =>
          debug(s"{waitingForLogin} LoginResponse received. With message id: $id")

          setLastId(id)
          myLastPrintedId = myLastId
          debug("{waitingForLogin} Become working")
          context.become(working(connection) orElse watching(connection))

        case LoginError(user, info) =>
          debug(s"{waitingForLogin} LoginError received. User name: $user, info: $info")

          printlnErr(s"$user login failed. Error message from server: $info")

          debug("{waitingForLogin} Stopping")
          context.stop(self)

        case ProcessingError(info) =>
          debug(s"{waitingForLogin} ProcessingError received: $info")

          printlnErr(s"Something bad happened while logging in. Error message from server: $info")

      }
  }

  private def working(connection: ActorRef): Receive = {
    case SendMessage(data) =>
      data match {
        case EXIT_COMMAND =>
          debug(s"{working} SendMessage with exit command received. Sending LogoutRequest to server")
          sendRequest(connection, LogoutRequest(userName))

        case cmd@SHOW_ME_MY_NAME_COMMAND =>
          debug("{working} SHOW_ME_MY_NAME_COMMAND received")
          printlnCmd(cmd, userName)

        case USER_LIST_COMMAND =>
          debug("{working} USER_LIST_COMMAND received. Sending UserListRequest to server")
          sendRequest(connection, UserListRequest())

        case text =>
          debug(s"{working} Text received: $text. Sending SendRequest to server")
          myMessagesSent.append(text)
          sendRequest(connection, SendRequest(myMessagesSent.size - 1, text))
      }

    case HttpResponse(_, entity, _, _) =>
      debug(s"{working} Received HttpResponse with entity $entity")
      receiveMessage(entity) {
        case FetchResponse(id) =>
          debug(s"{working} FetchResponse with id: $id received")
          setLastId(id)
          printAlignedMessages()

        case SendResponse(clientMessageId, timeStamp, id) =>
          debug(s"{working} SendResponse with " +
            s"clientMessageId: $clientMessageId, timeStamp: $timeStamp, id: $id received")
          setLastId(id)
          if (clientMessageId < myMessagesSent.size) {
            debug(s"{working} Storing message to LocalStorage")
            LocalStorage.storeMessage(Message(id = id, timeStamp = timeStamp,
              user = userName, data = myMessagesSent(clientMessageId.toInt)))
          }
          printAlignedMessages()

        case MessageListResponse(_, _, messages) =>
          debug(s"{working} Received MessageListResponse with messages: $messages")
          if (myWaitingTask != null) {
            debug("{working} Canceling myWaitingTask(this task needed to relaunch MessageListRequest)")
            myWaitingTask.cancel()
            myWaitingForMessages = false
          }
          debug("{working} Storing messages to LocalStorage")
          LocalStorage.storeMessages(messages)
          printAlignedMessages()

        case UserListResponse(users) =>
          debug(s"{working} Received UserListResponse with users: $users")
          printlnCmd(USER_LIST_COMMAND, users.mkString(", "))

        case ProcessingError(info) =>
          debug(s"{working} Received ProcessingError with info: $info")
          printlnErr(s"Something bad happened. Error message from server: $info")

        case LogoutResponse(_) =>
          debug("{working} LogoutResponse received. Stopping and closing connections")
          IO(Http) ! Http.CloseAll
          context.stop(self)

        case _ =>
          debug("{working} Invalid response. ")
          printlnErr("Invalid response from server. ")

      }

    case t: ReceiveTimeout =>
      debug("{working} Timer expired. Sending FetchRequest to server")
      sendRequest(connection, FetchRequest())
      if (!myWaitingForMessages && myLastPrintedId != myLastId) {
        debug("{working} We are waiting for messages now. Sending MessageListRequest to server")

        myWaitingForMessages = true
        val myself = self
        myWaitingTask = system.scheduler.scheduleOnce(WAITING_FOR_MESSAGES_TIMEOUT) {
          myself ! WaitingForMessagesExpired()
        }
        sendRequest(connection, MessageListRequest(lowerId = myLastPrintedId + 1, upperId = myLastId))
      }

    case WaitingForMessagesExpired() =>
      debug("{working} Waiting for messages expired")
      myWaitingForMessages = false
  }

  override def receive: Actor.Receive = {
    case Http.Connected(_, _) =>
      debug("{receive} Connection established. Watching connection")
      val connection = sender()
      context.watch(connection)

      debug("{receive} Sending request with protocol version. " +
        "After - sending LoginRequest and become waitingForProtocolAccepted")
      sendRequest(connection, PROTOCOL_VERSION)
      context.become(waitingForProtocolAccepted(connection) orElse watching(connection))
  }
}

