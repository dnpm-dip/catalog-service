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
  Monad,
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

/*
  def codeSystemInfos(
    implicit env: Env
  ): F[Seq[CodeSystem.Info]] 
*/

  def infos(
    implicit env: Env
  ): F[Seq[CodeSystemProvider.Info[Any]]] 

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
  import cats.syntax.flatMap._
  import cats.syntax.applicative._
  import cats.syntax.traverse._

  def codeSystem(
    uri: URI,
    version: Option[String],
    filters: Option[List[List[String]]]
  )(
    implicit
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[Any]]] =
    for {
      optCsp <- self.codeSystemProvider(uri)

      codeSystem =
        for {
          csp <- optCsp

          filter =
            filters match {
              case Some(nameGroups) if nameGroups.nonEmpty =>
                Some(
                  nameGroups
                    .map(
                      _.traverse(csp.filter(_))
                       .flatten
                       .reduce(_ or _)
                    )
                    .reduce(_ and _)
                )
              case _ => None
            }
          
          cs <-
            csp.get(version.getOrElse(csp.latestVersion))
              .map(
                cs =>
                  filter match {
                    case Some(f) => cs.filter(f)
                    case None    => cs
                  }                  
              )
        } yield cs
 
    } yield codeSystem


  def codeSystem[S](
    version: Option[String],
    filters: Option[List[List[String]]]
  )(
    implicit
    sys: Coding.System[S],
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[S]]] =
    self.codeSystem(sys.uri,version,filters)
      .map(
        _.map(_.asInstanceOf[CodeSystem[S]])
      )

/*
  def codeSystem(
    uri: URI,
    version: Option[String],
    filters: Option[List[String]]
  )(
    implicit
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[Any]]] =
    for {
      optCsp <- self.codeSystemProvider(uri)

      codeSystem =
        for {
          csp <- optCsp

          filter =
            filters match {
              case Some(names) if names.nonEmpty =>
                Some(
                  names
                    .traverse(csp.filter(_))
                    .flatten
                    .reduce(_ and _)
                )
              case _ => None
            }
          
          cs <-
            csp.get(version.getOrElse(csp.latestVersion))
              .map(
                cs =>
                  filter match {
                    case Some(f) => cs.filter(f)
                    case None    => cs
                  }                  
              )
        } yield cs
 
    } yield codeSystem


  def codeSystem[S](
    version: Option[String],
    filters: Option[List[String]]
  )(
    implicit
    sys: Coding.System[S],
    env: Env,
    F: Functor[F]
  ): F[Option[CodeSystem[S]]] =
    self.codeSystem(sys.uri,version,filters)
      .map(
        _.map(_.asInstanceOf[CodeSystem[S]])
      )
*/

/*
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
*/
}
