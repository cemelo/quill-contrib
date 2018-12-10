package io.getquill.context.streaming
import enumeratum.{Enum, EnumEntry}
import io.getquill.context.jdbc.JdbcContext

trait EnumDecoders { self: JdbcContext[_, _] =>
  implicit def enumDecoder[A <: EnumEntry](implicit enum: Enum[A]): Decoder[A] =
    decoder((index, row) => {
      val rawValue = row.getObject(index).toString
      enum.withName(rawValue)
    })
}
