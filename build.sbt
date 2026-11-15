ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version      := "0.1.0"

lazy val sparkDeps = Seq(
  "org.apache.spark" %% "spark-core" % "4.3.0" % "provided",
  "org.apache.spark" %% "spark-sql"  % "4.3.0" % "provided"
)

lazy val lintOptions = Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint:unused"
)

lazy val root = (project in file("."))
  .aggregate(mySparkApp)

lazy val mySparkApp = (project in file("workspace/spark-workshop"))
  .settings(
    name := "spark-workshop",
    libraryDependencies ++= sparkDeps,
    scalacOptions ++= lintOptions
  )