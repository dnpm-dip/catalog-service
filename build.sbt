
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "catalog-service"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "1.0-SNAPSHOT"


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
      dependencies.model
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
    val scalatest    = "org.scalatest" %% "scalatest"              % "3.1.1" % Test
    val slf4j        = "org.slf4j"     %  "slf4j-api"              % "1.7.32"
    val model        = "de.dnpm.dip"   %% "core"                   % "1.0-SNAPSHOT"
    val atc_impl     = "de.dnpm.dip"   %% "atc-impl"               % "1.0-SNAPSHOT" % Test
    val atc_package  = "de.dnpm.dip"   %% "atc-catalogs-packaged"  % "1.0-SNAPSHOT" % Test
    val icd10gm_impl = "de.dnpm.dip"   %% "icd10gm-impl"           % "1.0-SNAPSHOT" % Test
    val icdo3_impl   = "de.dnpm.dip"   %% "icdo3-impl"             % "1.0-SNAPSHOT" % Test
    val icd_package  = "de.dnpm.dip"   %% "icd-claml-packaged"     % "1.0-SNAPSHOT" % Test
    val hgnc_impl    = "de.dnpm.dip"   %% "hgnc-gene-set-impl"     % "1.0-SNAPSHOT" % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-feature",
//  "-language:existentials",
//  "-language:higherKinds",
//  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-deprecation",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++=
    Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

