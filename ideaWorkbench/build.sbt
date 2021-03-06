val commonSettings = Seq(
  name := "Fs2Workbench",
  scalaVersion := "2.12.3",
  libraryDependencies ++= Seq(
   "co.fs2" %% "fs2-core" % "0.10.0-M6",
    "co.fs2" %% "fs2-io" % "0.10.0-M6"
  ) ++ DefaultDependencies.UtilSettings.dependencies ++
    DefaultDependencies.MetaProgramming.dependencies ++
    DefaultDependencies.TestSettings.dependencies
//  keys.fork in Test := false,
//  keys.parallellExecution := false
)

lazy val ideaWorkbench = (project in file("."))
    .settings(commonSettings :_*)
