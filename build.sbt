scalaVersion := "2.9.2"

scalacOptions ++= Seq("-Ydependent-method-types")

libraryDependencies ++= Seq(
    "net.databinder" %% "unfiltered" % "0.6.4",
    "net.databinder" %% "unfiltered-netty-server" % "0.6.4",
    "org.scalatest" %% "scalatest" % "2.0.M4" % "test"
)
