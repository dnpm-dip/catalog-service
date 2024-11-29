package de.dnpm.dip.catalog.impl


import java.io.{
  InputStream,
}
import java.net.URI
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
  ValueSetProvider,
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

  private val cspMap: Map[URI,CodeSystemProvider[Any,Id,Applicative[Id]]] = 
    TrieMap.from(
      CodeSystemProvider.getInstances[Id]
        .map(csp => (csp.uri -> csp))
        .toList
    )

  private val vspMap: Map[URI,ValueSetProvider[Any,Id,Applicative[Id]]] = 
    TrieMap.from(
      ValueSetProvider.getInstances[Id]
        .map(vsp => (vsp.uri -> vsp))
        .toList
    )


  //---------------------------------------------------------------------------
  // Load CodeSystems as JSON from configured directory or other sources
  //---------------------------------------------------------------------------
  trait CodeSystemSource
  {
    def inputStreams: Seq[InputStream]
  }

  trait CodeSystemSourceSPI extends SPI[CodeSystemSource]

  object CodeSystemSource extends SPILoader[CodeSystemSourceSPI]

  private val loadedCodeSystems: Map[URI,Seq[CodeSystem[Any]]] =
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

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------


  private [impl] class Facade[F[_]] extends CatalogService[F,Applicative[F]]
  {

    import cats.syntax.applicative._
    import cats.syntax.functor._


    override def codeSystemInfos(
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
              csp.filters
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



    override def valueSetInfos(
      implicit F: Applicative[F]
    ): F[Seq[ValueSetProvider.Info]] =
      vspMap.values
        .map {
          vsp =>
            val latest = vsp.latest

            ValueSetProvider.Info(
              latest.name,
              latest.title,
              vsp.uri,
              vsp.versions.toList,
              vsp.latestVersion,
            )
        }
        .toSeq
        .pure
   
    override def valueSetProviders(
      implicit F: Applicative[F]
    ): F[Seq[ValueSetProvider[Any,Id,Applicative[Id]]]] =
      vspMap.values
        .toSeq
        .pure
    
    override def valueSetProvider(
      uri: URI
    )(
      implicit F: Applicative[F]
    ): F[Option[ValueSetProvider[Any,Id,Applicative[Id]]]] =
      vspMap.get(uri)
        .pure
    
    override def valueSetProvider[S](
      implicit
      sys: Coding.System[S],
      F: Applicative[F]
    ): F[Option[ValueSetProvider[S,Id,Applicative[Id]]]] =
      this.valueSetProvider(sys.uri)
        .map(
          _.map(_.asInstanceOf[ValueSetProvider[S,Id,Applicative[Id]]])
        )

  }

}
