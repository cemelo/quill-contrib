name := "quill-contrib"
version := "0.1"
scalaVersion := "2.12.7"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-unchecked",
                        "-deprecation",
                        "-feature",
                        "-language:implicitConversions",
                        "-language:higherKinds",
                        "-language:existentials",
                        "-Ypartial-unification",
                        "-Xmacro-settings:materialize-derivations")
)

lazy val `quill-contrib` =
  (project in file("."))
    .aggregate(`quill-refined`, `quill-enumeratum`)

lazy val `quill-enumeratum` =
  (project in file("quill-enumeratum"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq("com.beachape" %% "enumeratum" % "1.5.13", "io.getquill" %% "quill-jdbc" % "2.6.0")
    )

lazy val `quill-refined` =
  (project in file("quill-refined"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq("eu.timepit" %% "refined" % "0.9.3", "io.getquill" %% "quill-jdbc" % "2.6.0"))
