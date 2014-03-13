Simple chat application
======================

Server
------

Server based on Akka and Spray.
Protocol events marshalling and unmarshalling implemented via native scala xml.
To enable/disable logging edit src/main/resurces/application.conf

Client
------

Simple console client app.

### Commands:

Show current user name:
##### /me
Show all users:
##### /users 
Reconnect with new name:
##### /restart
Exit:
##### /q
