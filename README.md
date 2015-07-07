users
===

A simple REST user service written in Scala that demonstrates [spray](http://spray.io), [Slick 3](http://slick.typesafe.com/)
 and a few other Scala features (e.g. XML literals).

It is based on [spray-template](https://github.com/spray/spray-template) and is also inspired by [s4](https://github.com/jacobus/s4/).

In order to play with it just type `sbt run` and then open [http://localhost:8080](http://localhost:8080) in a web browser.

If you are facing any problems make sure that you have [sbt](http://www.scala-sbt.org/) installed properly.

In addition, a simple unit tests based on [specs2](https://etorreborre.github.io/specs2/) are provided. Just type `sbt test`.

It is a level two REST service according to [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html)
with some [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS) principles (multiple media types for the root resource; list of available resources for the root resource).