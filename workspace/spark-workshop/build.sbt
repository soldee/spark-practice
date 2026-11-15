name := "spark-workshop"
version := "0.1.0"
scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "4.3.0" % "provided",
    "org.apache.spark" %% "spark-sql"  % "4.3.0" % "provided"
)

scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint:unused",
    "-Xlint:adapted-args",
    "-Xlint:nullary-unit",
    "-Xlint:inaccessible"
)