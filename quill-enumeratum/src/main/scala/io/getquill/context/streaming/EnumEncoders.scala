package io.getquill.context.streaming
import java.sql.Types
import enumeratum.{Enum, EnumEntry}
import io.getquill.context.jdbc.JdbcContext

trait EnumEncoders { self: JdbcContext[_, _] =>

  implicit def enumEncoder[A <: EnumEntry](implicit enum: Enum[A]): Encoder[A] =
    encoder(Types.VARCHAR, (index, value, row) => {
      row.setObject(index, value, Types.OTHER)
    })
}
