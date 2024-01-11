package de.dnpm.dip.catalog.test


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
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

    catalogService.infos.map(_.uri) must contain allOf (
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

     catalogService
       .codeSystem[HGNC](
         version = None,
         filters = None
       ) must be (defined)

  }


  "CodeSystem ICD-O-3-T" must "have be correctly retrieved and filtered" in {

    import scala.util.matching.Regex
    import de.dnpm.dip.coding.icd.ClassKinds.{Block,Category}

    val icdO3Tcategory = """C\d{2}-C\d{2}|C\d{2}(.\d)?""".r

    val icdO3T =
      catalogService
        .codeSystem[ICDO3](
          version = None,
          filters =
            Some(
              List(
                List(ICDO3.topographyFilter.name),
                List(
                  ICDO3.filterByClassKind(Block).name,
                  ICDO3.filterByClassKind(Category).name
                )
              )
            )
        )

    all (icdO3T.value.concepts.map(_.code.value)) must fullyMatch regex icdO3Tcategory
     
  }


}
