package io.getquill.examples

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.util.LoadConfig
import io.getquill.{ JdbcContextConfig, Literal, PostgresZioJdbcContext }
import zio.{ Has, Runtime, Task, ZLayer }
import zio.console.putStrLn

import java.io.Closeable
import java.sql.Connection
import javax.sql.DataSource
import io.getquill._

object PlainAppDataSource2 {

  object MyPostgresContext extends PostgresZioJdbcContext(Literal)
  import MyPostgresContext._

  case class Person(name: String, age: Int)

  def hikariConfig = new HikariConfig(JdbcContextConfig(LoadConfig("testPostgresDB")).configProperties)
  def hikariDataSource: DataSource with Closeable = new HikariDataSource(hikariConfig)

  val zioDS: ZLayer[Any, Throwable, Has[DataSource]] =
    Task(hikariDataSource).toLayer

  def main(args: Array[String]): Unit = {
    val people = quote {
      query[Person].filter(p => p.name == "Alex")
    }
    val qzio =
      MyPostgresContext.run(people)
        .tap(result => putStrLn(result.toString))
        .provideCustomLayer(zioDS)

    Runtime.default.unsafeRun(qzio)
    ()
  }
}
