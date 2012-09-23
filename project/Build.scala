import sbt._
import sbt.Keys._

object Build extends Build {

  lazy val root = Project("auth", file("."))
    .aggregate(blogpost, demo)

  lazy val blogpost = Project("blogpost", file("blogpost")).settings(commonSettings: _*)

  lazy val demo = Project("demo", file("demo")).settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      "net.databinder" %% "unfiltered" % "0.6.4",
      "net.databinder" %% "unfiltered-netty-server" % "0.6.4"
    )
  ): _*)

  def commonSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.9.2",
    scalacOptions ++= Seq("-Ydependent-method-types"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.0.M4" % "test"
    )
  )

}
