package de.dnpm.dip.catalog.api


import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  ValueSet
}


@annotation.implicitNotFound(
"Couldn't resolve implicit CodeSystems for default injection. Ensure implicit CodeSystems are in scope for all types in ${CS}."
)
trait CodeSystems[CS]{
  val values: List[CodeSystem[Any]]
}

object CodeSystems
{

  import shapeless.{HList, ::, HNil, Generic}

  def apply[CS <: Product](implicit cs: CodeSystems[CS]) = cs

  implicit def genericCodeSystem[CS, CSpr](
    implicit
    gen: Generic.Aux[CS,CSpr],
    genCs: CodeSystems[CSpr]
  ): CodeSystems[CS] =
    new CodeSystems[CS]{
      val values = genCs.values
    }

  implicit def hlistCodeSystems[H, T <: HList](
    implicit
    hcs: CodeSystem[H],
    tcs: CodeSystems[T]
  ): CodeSystems[H :: T] =
    new CodeSystems[H :: T]{
      val values = hcs :: tcs.values
    }

  implicit def productHeadHListCodeSystems[H <: Product, T <: HList](
    implicit
    hcs: CodeSystems[H],
    tcs: CodeSystems[T]
  ): CodeSystems[H :: T] =
    new CodeSystems[H :: T]{
      val values = hcs.values ++ tcs.values
    }

  implicit val hnilCodeSystems: CodeSystems[HNil] =
    new CodeSystems[HNil]{ 
      val values = Nil
    }

}


