package ru.spbau.network.montsev.chat.client

import ru.spbau.network.montsev.chat.server.Message

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:02 AM
 **/
object Printer {
  def printlnErr(err: String) {
    println(s"[ERROR] $err. ")
  }

  def printlnCmd(cmd: String, info: String) {
    println(s"[COMMAND] {$cmd}: $info")
  }

  def printlnMsg(msg: Message) {
    printlnMsg(msg.user, msg.timeStamp, msg.data)
  }

  def printlnMsg(user: String, timeStamp: String, msg: String) {
    println(s"[$timeStamp] {$user}: $msg")
  }
}
