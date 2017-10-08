name := "Fs2Workbench"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
 "co.fs2" %% "fs2-core" % "0.10.0-M6",
  "co.fs2" %% "fs2-io" % "0.10.0-M6"
) ++ DefaultDependencies.UtilSettings.dependencies
