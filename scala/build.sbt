name := "latitude-coding-test"

version := "0.1"

enablePlugins(DockerPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.0",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.0.0",
      "org.typelevel" %% "cats-effect" % "2.1.1",
      "co.fs2" %% "fs2-core" % "2.2.1",
      "co.fs2" %% "fs2-io" % "2.2.1",
      "com.lihaoyi" %% "utest" % "0.7.2" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    mainClass in (Compile, run) := Some("latitude.main"),
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("openjdk:8-jre")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },
    imageNames in docker := Seq(ImageName(s"trudolf/latitude-coding-test:latest"))
  )
