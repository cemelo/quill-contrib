package io.getquill.context.streaming
import java.sql.Types.ARRAY
import eu.timepit.refined.api.Refined
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.encoding.ArrayEncoding
import shapeless.<:!<
import shapeless.tag.{@@, Tagged}

trait RefinedEncoders extends ArrayEncoding { self: JdbcContext[_, _] =>

  implicit def refinedTypeEncoder[T, P](implicit encoder: Encoder[T]): BaseEncoder[T Refined P] =
    (index, refinedValue, row) => encoder(index - 1, refinedValue.value, row)

  implicit def refinedTypeSeqArrayEncoder[T, P, C[X] <: Seq[X]](implicit underlying: Encoder[Seq[T]],
                                                                ev: T <:!< Refined[_, _]): Encoder[Seq[T Refined P]] =
    encoder(ARRAY, (idx: Index, seq: Seq[T Refined P], row: PrepareRow) => {
      underlying(idx - 1, seq.map(_.value), row)
    })

  implicit def taggedTypeEncoder[T, P](implicit encoder: Encoder[T], ev: T <:!< Tagged[_]): BaseEncoder[T @@ P] =
    (index, refinedValue, row) => encoder(index - 1, refinedValue, row)

  implicit def taggedTypeSeqArrayEncoder[T, P, C[X] <: Seq[X]](implicit underlying: Encoder[Seq[T]],
                                                                ev: T <:!< Tagged[_]): Encoder[Seq[T @@ P]] =
    encoder(ARRAY, (idx: Index, seq: Seq[T @@ P], row: PrepareRow) => {
      underlying(idx - 1, seq.map(_.asInstanceOf[T]), row)
    })
}
