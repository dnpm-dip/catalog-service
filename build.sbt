// build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt

import scala.util.Properties.envOrElse


name := "catalog-service"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version      := envOrElse("VERSION","1.0.0")

val ownerRepo  = envOrElse("REPOSITORY","dnpm-dip/catalog-service").split("/")
ThisBuild / githubOwner      := ownerRepo(0)
ThisBuild / githubRepository := ownerRepo(1)


//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    publish / skip := true
  )
  .aggregate(
     api,
     impl,
     tests
  )

lazy val api = project
  .settings(
    name := "catalog-service-api",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.core
    )
  )

lazy val impl = project
  .settings(
    name := "catalog-service-impl",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
    )
  )
  .dependsOn(api)


lazy val tests = project
  .settings(
    name := "tests",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.atc_impl,
      dependencies.atc_package,
      dependencies.hgnc_impl,
      dependencies.icd10gm_impl,
      dependencies.icdo3_impl,
      dependencies.icd_package,
      dependencies.rd_model,
      dependencies.ordo,
      dependencies.alpha_id_se
    ),
    publish / skip := true
  )
  .dependsOn(
    api,
    impl % Test
  )



//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val scalatest    = "org.scalatest" %% "scalatest"             % "3.2.18" % Test
    val core         = "de.dnpm.dip"   %% "core"                  % "1.0.0"
    val atc_impl     = "de.dnpm.dip"   %% "atc-impl"              % "1.0.0" % Test
    val atc_package  = "de.dnpm.dip"   %% "atc-catalogs-packaged" % "1.0.0" % Test
    val icd10gm_impl = "de.dnpm.dip"   %% "icd10gm-impl"          % "1.0.0" % Test
    val icdo3_impl   = "de.dnpm.dip"   %% "icdo3-impl"            % "1.0.0" % Test
    val icd_package  = "de.dnpm.dip"   %% "icd-claml-packaged"    % "1.0.0" % Test
    val hgnc_impl    = "de.dnpm.dip"   %% "hgnc-gene-set-impl"    % "1.0.0" % Test
    val rd_model     = "de.dnpm.dip"   %% "rd-dto-model"          % "1.0.0" % Test
    val ordo         = "de.dnpm.dip"   %% "orphanet-ordo"         % "1.0.0" % Test
    val alpha_id_se  = "de.dnpm.dip"   %% "alpha-id-se"           % "1.0.0" % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


// Compiler options from: https://alexn.org/blog/2020/05/26/scala-fatal-warnings/
lazy val compilerOptions = Seq(
  // Feature options
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ymacro-annotations",

  // Warnings as errors!
  "-Xfatal-warnings",

  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wunused:implicits",
  "-Wvalue-discard",
)


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.githubPackages("dnpm-dip"),
    Resolver.sonatypeCentralSnapshots
  )
)

