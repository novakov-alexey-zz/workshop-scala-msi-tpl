import sbtrelease.ReleaseStateTransformations._

name := "workshop-scala-msi"
organization in ThisBuild := "org.scalamsi"
scalaVersion in ThisBuild := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      //workshop dependencies
      cats,

      doobieCore,
      doobiePostgres,
      doobieScalatest,

      http4sBlaze,
      http4sCirce,
      http4sDsl,
      http4sClient,

      circeGeneric,
      circeJava8,

      scalaLogging,
      logbackClassic,

      pureConfig,
      refinedPureconfig,

      macwire,
      macwireUtil,

      scalaTest,
      testContainers,
      testContainersPostgres,
    ),
    mainClass in Compile := Some("org.scalamsi.Main"),
    dockerBaseImage := "openjdk:8-jre-alpine",
    Test / fork := true,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )
  .enablePlugins(AshScriptPlugin)

// https://tpolecat.github.io/2017/04/25/scalac-flags.html
// https://docs.scala-lang.org/overviews/compiler-options/index.html
scalacOptions ++= Seq(
  "-explaintypes",                     // Explain type errors in more detail.
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-target:jvm-1.8",
  "-unchecked",
  "-Xfuture",
//  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
//  "-Ywarn-unused",
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
)

scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")

releaseProcess := Seq.empty[ReleaseStep]
releaseProcess ++= (if (sys.env.contains("RELEASE_VERSION_BUMP"))
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease
  )
else Seq.empty[ReleaseStep])
releaseProcess ++= (if (sys.env.contains("RELEASE_PUBLISH"))
  Seq[ReleaseStep](inquireVersions, setNextVersion, commitNextVersion)
else Seq.empty[ReleaseStep])
