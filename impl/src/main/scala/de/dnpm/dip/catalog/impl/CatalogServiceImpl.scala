package de.dnpm.dip.catalog.impl


import java.io.{
  InputStream,
  FileInputStream
}
import java.net.URI
import java.util.ServiceLoader
import scala.collection.concurrent.{
  Map,
  TrieMap
}
import play.api.libs.json.Json
import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.catalog.api.{
  CatalogService,
  CatalogServiceProvider
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  ValueSet
}
import de.dnpm.dip.util.{
  SPI,
  SPILoader,
  Logging
}


class CatalogServiceProviderImpl extends CatalogServiceProvider
{

  def getInstance[F[_]]: CatalogServiceImpl.Facade[F] = {
    new CatalogServiceImpl.Facade[F]
  }

}


private[impl] object CatalogServiceImpl extends Logging
{

//  private val defaultCodeSystems: Map[URI,CodeSystem[Any]] =
//    TrieMap.empty


  private val cspMap: Map[URI,CodeSystemProvider[Any,Id,Applicative[Id]]] = {

    import scala.jdk.StreamConverters._

    TrieMap.from(
      CodeSystemProvider.getInstances[Id]
        .map(csp => (csp.uri -> csp))
        .toList
    )

  }


  //---------------------------------------------------------------------------
  // Load CodeSystems as JSON from configured directory or other sources
  //---------------------------------------------------------------------------
  trait CodeSystemSource
  {
    def inputStreams: Seq[InputStream]
  }

  trait CodeSystemSourceSPI extends SPI[CodeSystemSource]

  object CodeSystemSource extends SPILoader[CodeSystemSourceSPI]

  private val loadedCodeSystems: Map[URI,Seq[CodeSystem[Any]]] = {

    import scala.jdk.StreamConverters._

    TrieMap.from(
      CodeSystemSource.getInstances
        .toSeq
        .flatMap(_.inputStreams.map(Json.parse))
        .map(Json.fromJson[CodeSystem[Any]](_))
        .tapEach(
          _.fold(
            errs => log.error(s"JSON validation errors occurred while loading CodeSystems: ${errs}"),
            s => ()
          )
        )
        .map(_.get)
        .groupBy(_.uri)
    )
  } 

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------


  private [impl] class Facade[F[_]] extends CatalogService[F,Applicative[F]]
  {

    import cats.syntax.applicative._
    import cats.syntax.functor._

/*
    override def withCodeSystems(
      css: Seq[CodeSystem[Any]]
    ): CatalogService[F,Applicative[F]] = {

      defaultCodeSystems ++=
        css.map(cs => cs.uri -> cs)

      this  
    }
*/

/*
    override def codeSystemInfos(
      implicit F: Applicative[F]
    ): F[Seq[CodeSystem.Info]] = {
       (
        cspMap.values
          .flatMap( csp =>
            csp.versions
              .toList
              .map(
                v => csp.get(v).get
              )
          )
          ++ loadedCodeSystems.values.flatten
       )
       .map(cs =>
         CodeSystem.Info(
           cs.name,
           cs.title,
           cs.uri,
           cs.version
         )
       )
       .toSeq
       .pure
    }
*/ 

    override def infos(
      implicit F: Applicative[F]
    ): F[Seq[CodeSystemProvider.Info[Any]]] =
      cspMap.values
        .map {
          csp =>
            val latest = csp.latest

            CodeSystemProvider.Info[Any](
             latest.name,
             latest.title,
             csp.uri,
             csp.versions.toList,
             csp.latestVersion,
             csp.filters,
           )
        }
        .toSeq
        .pure
   

    override def codeSystemProviders(
      implicit F: Applicative[F]
    ): F[Seq[CodeSystemProvider[Any,Id,Applicative[Id]]]] =
      cspMap.values
        .toSeq
        .pure
    
    override def codeSystemProvider(
      uri: URI
    )(
      implicit F: Applicative[F]
    ): F[Option[CodeSystemProvider[Any,Id,Applicative[Id]]]] =
      cspMap.get(uri)
        .pure
    
    override def codeSystemProvider[S](
      implicit
      sys: Coding.System[S],
      F: Applicative[F]
    ): F[Option[CodeSystemProvider[S,Id,Applicative[Id]]]] =
      this.codeSystemProvider(sys.uri)
        .map(
          _.map(_.asInstanceOf[CodeSystemProvider[S,Id,Applicative[Id]]])
        )

    override def versionsOf(
      uri: URI
    )(
      implicit F: Applicative[F]
    ): F[Seq[String]] = {
      cspMap.get(uri)
        .map(_.versions.toList)
        .orElse(
          loadedCodeSystems.get(uri)
            .map(_.map(_.version).flatten)
        )
        .getOrElse(List.empty)
        .pure
    }

  }

}
