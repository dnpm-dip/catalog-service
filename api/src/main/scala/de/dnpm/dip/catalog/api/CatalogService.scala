package de.dnpm.dip.catalog.api


import java.net.URI
import scala.util.Either
import scala.concurrent.{
  Future,
  ExecutionContext
}
import cats.{
  Applicative,
  Functor,
  Id
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider,
  ValueSet
}


trait CatalogServiceProvider extends SPIF[
  ({ type Service[F[_]] = CatalogService[F,Applicative[F]] })#Service
]

object CatalogService extends SPILoaderF[CatalogServiceProvider]


trait CatalogService[F[_],Env]
{
  self =>

/*
  import CatalogService._

  def withCodeSystems(
    cs: Seq[CodeSystem[Any]]
  ): CatalogService[F,Env]


  def withImplicits[CS <: Product](
    implicit cs: CodeSystems[CS]
  ): CatalogService[F,Env] =
    self.withCodeSystems(cs.values)
*/


  def versionsOf(
    uri: URI
  )(
    implicit env: Env
  ): F[Seq[String]]

  def versionsOf[S](
    implicit
    sys: Coding.System[S],
    env: Env
  ): F[Seq[String]] =
    self.versionsOf(sys.uri)


  def codeSystemProviders(
    implicit env: Env
  ): F[Seq[CodeSystemProvider[Any,Id,Applicative[Id]]]]

  def codeSystemProvider(
    uri: URI
  )(
    implicit env: Env
  ): F[Option[CodeSystemProvider[Any,Id,Applicative[Id]]]]

  def codeSystemProvider[S](
    implicit
    sys: Coding.System[S],
    env: Env
  ): F[Option[CodeSystemProvider[S,Id,Applicative[Id]]]]



  import cats.syntax.functor._

  def codeSystemInfos(
    implicit env: Env
  ): F[Seq[CodeSystem.Info]] 

  def codeSystem(
    uri: URI,
    version: Option[String]
  )(
    implicit
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[Any]]] =
    self.codeSystemProvider(uri)
      .map(
        _.flatMap(
          csp => csp.get(version.getOrElse(csp.latestVersion))
        )
      )

  def codeSystem[S](
    version: Option[String]
  )(
    implicit
    sys: Coding.System[S],
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[S]]] =
    self.codeSystem(sys.uri,version)
      .map(
        _.map(_.asInstanceOf[CodeSystem[S]])
      )


/*
  def valueSets(
    implicit env: Env
  ): F[Seq[ValueSet.Info]]


  def valueSet(
    uri: URI,
    version: Option[String]
  )(
    implicit env: Env
  ): F[Option[ValueSet[Any]]]

  def valueSet[S](
    version: Option[String]
  )(
    implicit
    sys: Coding.System[S],
    env: Env
  ): F[Option[ValueSet[S]]]
*/

}
