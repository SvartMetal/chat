## Simple chat application ##

### Running chat ###

Clone this repository. `cd` into it and start [sbt](http://www.scala-sbt.org).
Simply type `run` and choose `ChatServiceApp` for server or `ClientApp` for client.

### Server ###

Server based on Akka and Spray.
Protocol events marshalling and unmarshalling implemented using native scala xml.
To enable/disable logging edit src/main/resurces/application.conf.

### Client ###

Simple console client app.
You can run some commands and chat with your friends!

### Commands ###

Show current user name:
```   /me
```
Show all users:
```  /users
```
Reconnect with new name:
```  /restart
```
Exit:
```  /q
```
