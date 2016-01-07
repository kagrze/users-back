version       := "0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

val akkaV = "2.4.1"
val sprayV = "1.3.2"

libraryDependencies ++= {  
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.slick"  %%  "slick"         % "3.1.1",
    "com.h2database"      %   "h2"            % "1.4.190",
    "org.slf4j"           %   "slf4j-nop"     % "1.7.13",
    "org.specs2"          %%  "specs2-core"   % "2.3.13" % "test"
  )
}
