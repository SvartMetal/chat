package ru.spbau.network.montsev.chat.protocol

import scala.util.Try

/**
 * Author: Mikhail Montsev
 * Date: 3/5/14
 * Time: 3:44 AM
 **/
trait Protocol {

  def apply(msg: String): Try[Event]

  def apply(event: Event): String
}
