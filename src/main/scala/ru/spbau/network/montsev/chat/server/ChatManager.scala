package ru.spbau.network.montsev.chat.server

import akka.actor.{ActorLogging, Actor, Props}

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:43 AM
 **/
object ChatManager {
  case class Login(user: String)
  case class Logout(user: String)
  case class LoginOk(user: String)
  case class LoginError(info: String)
  case class LogoutOk(user: String)
  case class LogoutError(info: String)
  case class UserListRequest()
  case class UserListResponse(users: List[String])

  def props() = Props(new ChatManager)
}

class ChatManager extends Actor with ActorLogging {
  import ChatManager._

  private def debug(text: String) {
    log.debug(s"[ChatManager] $text")
  }

  private val myUsers = scala.collection.mutable.Set[String]()

  override def receive: Receive = {
    case Login(user) =>
      debug(s"{receive} Login received. User name is: $user")
      if (myUsers.contains(user)) {
        debug("{receive} User is engaged. Sending LoginError")
        sender ! LoginError("Login failed. User name is already used. ")
      } else {
        debug("{receive} Login success. Sending LoginOk")
        myUsers += user
        sender ! LoginOk(user)
      }

    case Logout(user) =>
      debug(s"{receive} Logout received. User name is: $user")
      if (myUsers.contains(user)) {
        debug("{receive} Logout success. Sending LogoutOk")
        myUsers -= user
        sender ! LogoutOk(user)
      } else {
        debug("{receive} Logout failed. Sending LogoutError")
        sender ! LogoutError("Logout failed. ")
      }

    case UserListRequest() =>
      debug(s"{receive} UserListRequest received. Sending UserListResponse. Users: $myUsers")
      sender ! UserListResponse(myUsers.toList)
  }
}
