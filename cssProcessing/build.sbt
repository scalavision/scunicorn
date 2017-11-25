/*
inThisBuild(Seq(
    scalaOrganization := "org.typelevel",
    scalaVersion := "2.12.4-bin-typelevel-4"
))
*/

val commonSettings = Seq(
  name := "CssProcessing",
//  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-value-discard"
//    "-Ywarn-unused-import"
   ),

  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console)),
  libraryDependencies ++= Seq(
   "com.lihaoyi" %% "ammonite-ops" % "1.0.3",
    "org.scodec" %% "scodec-stream" % "1.1.0-M8"
  ) ++ DefaultDependencies.UtilSettings.dependencies ++
    DefaultDependencies.MetaProgramming.dependencies ++
    DefaultDependencies.TestSettings.dependencies ++
    DefaultDependencies.IODefault.dependencies ++
    DefaultDependencies.TypeLevel.dependencies

//  keys.fork in Test := false,
//  keys.parallellExecution := false
)

lazy val testSettings = Seq(
  parallelExecution in Test := false
//  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
//  publishArtifact in Test := true
)

lazy val cssProcessing = (project in file("."))
    .settings(commonSettings :_*)
    .settings(testSettings :_*)
