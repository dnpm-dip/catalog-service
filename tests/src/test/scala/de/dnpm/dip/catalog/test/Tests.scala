package de.dnpm.dip.catalog.test


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.Inspectors._
import cats.Id
import de.dnpm.dip.model.{
  Gender,
  Therapy,
  VitalStatus
}
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.icd.{ICD10GM,ICDO3}
import de.dnpm.dip.catalog.api.CatalogService


class Tests extends AnyFlatSpec
{

  val catalogService =
    CatalogService.getInstance[Id]
      .get


  "All expected CodeSystems" must "have been loaded" in {

    catalogService.codeSystemInfos.map(_.uri) must contain allOf (
      Coding.System[ATC].uri,
      Coding.System[HGNC].uri,
      Coding.System[ICD10GM].uri,
      Coding.System[ICDO3].uri,
      Coding.System[Gender.Value].uri,
      Coding.System[VitalStatus.Value].uri,
      Coding.System[Therapy.Status.Value].uri
    )

  }


  "CodeSystem HGNC" must "be defined" in {

     catalogService.codeSystem[HGNC](version = None) must be (defined)

  }


}
