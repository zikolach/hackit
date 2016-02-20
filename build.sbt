import sbt.Project.projectToRef

def commonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val root = (project in file("."))
  .aggregate(frontend, backend)

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0",
      "com.lihaoyi" %%% "upickle" % "0.3.7",
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test"
    )
  )
  .dependsOn(sharedJs)

lazy val backend = (project in file("backend"))
  .settings(Revolver.settings: _*)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {
      val akkaVersion = "2.4.1"
      val akkaStreamVersion = "2.0.1"
      val upickleVersion = "0.3.7"
      val specs2Version = "3.6.6"
      Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
        "com.lihaoyi" %% "upickle" % upickleVersion,
        "org.specs2" %% "specs2-core" % specs2Version % "test"
      )
    },
    (resourceGenerators in Compile) <+=
      (fastOptJS in Compile in frontend, packageScalaJSLauncher in Compile in frontend)
        .map((f1, f2) => Seq(f1.data, f2.data)),
    watchSources <++= (watchSources in frontend)
  )
  .dependsOn(sharedJvm)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings: _*)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js