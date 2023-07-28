package de.dnpm.dip.catalog.impl


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import scala.util.Success
import cats.Id
import de.dnpm.dip.catalog.api.CatalogService


class CatalogServiceTests extends AnyFlatSpec
{

  val serviceTry = CatalogService.getInstance[Id]
 
  "CatalogService" must "have been successfully loaded" in {
    serviceTry must be (a [Success[_]])
  }


}
