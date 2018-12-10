package io.getquill.context.streaming
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import eu.timepit.refined.api.{Refined, RefType, Validate}
import io.getquill.context.jdbc.JdbcContext
import io.getquill.util.Messages.fail
import shapeless.tag.@@

trait RefinedDecoders { self: JdbcContext[_, _] =>

  implicit def refinedTypeDecoder[T, P](implicit d: Decoder[T],
                                        validate: Validate[T, P],
                                        refType: RefType[Refined]): Decoder[T Refined P] =
    decoder((index, row) => {
      refType.refine(d(index - 1, row)) match {
        case Left(err)           => throw new IllegalArgumentException(err)
        case Right(refinedValue) => refinedValue
      }
    })

  implicit def arrayTraversableRefinedTypeDecoder[T: ClassTag, P, C[X] <: Traversable[X]](
    implicit validate: Validate[T, P],
    refType: RefType[Refined],
    cbf: CanBuildFrom[C[T], T Refined P, C[T Refined P]]
  ): Decoder[C[T Refined P]] =
    decoder((idx: Index, row: ResultRow) => {
      val arr = row.getArray(idx)
      if (arr == null) cbf().result()
      else
        arr.getArray
          .asInstanceOf[Array[AnyRef]]
          .foldLeft(cbf()) {
            case (b, unrefined: T) =>
              refType.refine[P](unrefined.asInstanceOf[T]) match {
                case Left(err)           => fail(err)
                case Right(refinedValue) => b += refinedValue
              }
            case (_, _) =>
              fail("Wrong type fetched from the database")
          }
          .result()
    })

  implicit def taggedTypeDecoder[T, P](implicit d: Decoder[T],
                                       validate: Validate[T, P],
                                       refType: RefType[@@]): Decoder[T @@ P] =
    decoder((index, row) => {
      refType.refine(d(index - 1, row)) match {
        case Left(err)           => fail(err)
        case Right(refinedValue) => refinedValue
      }
    })

  implicit def arrayTraversableTaggedTypeDecoder[T: ClassTag, P, C[X] <: Traversable[X]](
    implicit validate: Validate[T, P],
    refType: RefType[@@],
    cbf: CanBuildFrom[C[T], T @@ P, C[T @@ P]]
  ): Decoder[C[T @@ P]] =
    decoder((idx: Index, row: ResultRow) => {
      val arr = row.getArray(idx)
      if (arr == null) cbf().result()
      else
        arr.getArray
          .asInstanceOf[Array[AnyRef]]
          .foldLeft(cbf()) {
            case (b, unrefined: T) =>
              refType.refine[P](unrefined.asInstanceOf[T]) match {
                case Left(err)           => fail(err)
                case Right(taggedValue) => b += taggedValue
              }
            case (_, _) =>
              fail("Wrong type fetched from the database")
          }
          .result()
    })
}
