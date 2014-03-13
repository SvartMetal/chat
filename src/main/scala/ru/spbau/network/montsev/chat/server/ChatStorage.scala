package ru.spbau.network.montsev.chat.server

import scala.collection.concurrent.TrieMap
import spray.http.DateTime
import scala.util.Try
import java.util.concurrent.atomic.AtomicLong

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:45 AM
 **/
object ChatStorage {
  private val myStorage = TrieMap[Long, Message]()
  private val myLastId: AtomicLong = new AtomicLong(-1)
  private val myCurrentId: AtomicLong = new AtomicLong(-1)

  private def timeStamp = DateTime.now.toIsoDateTimeString

  def storeMessage(user: String, data: String): Message = {
    val curId = myCurrentId.incrementAndGet()
    val msg = Message(id = curId, timeStamp = timeStamp, user = user, data = data)
    myStorage.put(curId, msg)
    myLastId.incrementAndGet()
    msg
  }

  def messageById(id: Long) = Try {
    myStorage.get(id).get
  }

  def lastId = myLastId.get()

  def messageList(lowerId: Long, upperId: Long): List[Message] = {
    (for (i <- lowerId to upperId) yield {
      myStorage.getOrElse(i, Message.invalid)
    }).filterNot(_ == Message.invalid).toList
  }

}
