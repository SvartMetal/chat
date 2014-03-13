package ru.spbau.network.montsev.chat.client

import ru.spbau.network.montsev.chat.server.Message

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:02 AM
 **/
object LocalStorage {
  private var myMessages = Map[Long, Message]()

  def storeMessage(msg: Message) {
    myMessages += msg.id -> msg
  }

  def storeMessages(messages: List[Message]) {
    myMessages ++= messages.map(msg => msg.id -> msg)
  }

  def allMessagesAlignedFromId(id: Long, lastId: Long): (Long, List[Message]) = {
    def helper(acc: List[Message], curId: Long): (Long, List[Message]) = {
      if (curId > lastId) {
        (lastId, acc.reverse)
      } else {
        myMessages.get(curId) match {
          case Some(msg) =>
            helper(msg :: acc, curId + 1)
          case _ =>
            (if (curId != id) curId - 1 else id, acc.reverse)
        }
      }
    }

    helper(Nil, id + 1)
  }

  def contains(id: Long) = myMessages.contains(id)

  def get(id: Long) = myMessages.get(id)
}
