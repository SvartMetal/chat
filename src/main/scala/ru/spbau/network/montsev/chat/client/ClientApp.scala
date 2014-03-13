package ru.spbau.network.montsev.chat.client

import akka.actor.ActorSystem
import ru.spbau.network.montsev.chat.protocol.ProtocolFactory
import scala.util.Success
import ru.spbau.network.montsev.chat.server.Settings._
import ru.spbau.network.montsev.chat.client.Client.Stop

/**
 * Author: Mikhail Montsev
 * Date: 3/13/14
 * Time: 2:02 AM
 **/
object ClientApp extends App {

  import Printer._
  import Commands._
  import ClientSettings._

  val system = ActorSystem(CLIENT)

  ProtocolFactory(PROTOCOL_VERSION) match {
    case Success(protocol) =>
      var userName = readLine("Type your name please... ")

      var clientHandler = system.actorOf(Client.props(userName, PROTOCOL_VERSION, HOST, PORT, protocol))

      var msg = ""

      while (msg != EXIT_COMMAND) {
        msg = readLine()
        if (msg == RESTART_CONNECTION_COMMAND) {
          clientHandler ! Stop()
          userName = readLine("Type your name again...")
          clientHandler = system.actorOf(Client.props(userName, PROTOCOL_VERSION, HOST, PORT, protocol))
        } else if (msg.trim() != "") {
          clientHandler ! Client.SendMessage(msg)
        }
      }

    case _ =>
      printlnErr("Could not find protocol. Protocol version is invalid")
  }

  system.shutdown()
  system.awaitTermination()

}
