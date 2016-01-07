users-back
===

A simple REST service written in Scala that demonstrates [Akka](http://akka.io/), [spray](http://spray.io), [Slick 3](http://slick.typesafe.com/) and a few Scala language features (e.g. XML literals).

It is based on [spray-template](https://github.com/spray/spray-template) and is also inspired by [s4](https://github.com/jacobus/s4/).

The project is built with [sbt](http://www.scala-sbt.org/). In order to play with it just type `sbt run`, wait a while, and then open [http://localhost:8080](http://localhost:8080) in a web browser. Optionally you can test the service with [users-front](https://github.com/kagrze/users-front) client.

In addition, a simple unit tests based on [specs2](https://etorreborre.github.io/specs2/) are provided. Just type `sbt test`.

It is a level two REST service according to [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html)
with some [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS) principles (multiple media types for the root resource; list of available resources for the root resource).