package ru.spbau.network.montsev.chat.server

import scala.xml.{Node, XML}
import scala.xml.Utility.trim

/**
 * Author: Mikhail Montsev
 * Date: 3/8/14
 * Time: 3:29 AM
 **/

object Message {
  val invalid = Message(-1, "", "", "")

  def fromString(msg: String) = fromXML(XML.loadString(msg))

  def fromXML(elem: Node) = trim(elem) match {
    case <msg><id>{id}</id><time>{time}</time><name>{user}</name><data>{data}</data></msg> =>
      Message(id.text.toLong, time.text, user.text, data.text)
  }

}

case class Message(id: Long, timeStamp: String, user: String, data: String) {
  def toXML = {
    <msg>
    <id>{id}</id>
    <time>{timeStamp}</time>
    <name>{user}</name>
    <data>{data}</data>
    </msg>
  }
}


