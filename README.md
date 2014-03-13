Simple chat application
======================

Server
------

Server based on Akka and Spray.
Protocol events un/marshalling implemented via xml.
To enable logging see src/main/resurces/application.conf

Client
------

Simple console client app.

## Commands:

Show current user name:
### /me

Show all users:
### /users 

Reconnect with new name:
### /restart

Exit:
### /q
