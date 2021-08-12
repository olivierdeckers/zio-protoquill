package io.getquill

import scala.quoted._
import java.time.format.DateTimeFormatter
import io.getquill.util.Format
import io.getquill.util.LoadModule
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import io.getquill.util.CommonExtensions.Either._

/**
 * Trait that allows usage of 'static' block. Can declared one of these and use similar to encoders
 * but it needs to be compiled in a previous compilation unit and a global static.
 * TODO More explanation
 */
trait StaticSplice[T]:
  def apply(value: T): String

object StaticSplice:
  import io.getquill.metaprog.Extractors._

  object Summon:
    def apply[T: Type](using Quotes): Either[String, StaticSplice[T]] =
      import quotes.reflect._
      val out =
        for {
          summonValue <- Expr.summon[StaticSplice[T]].toEitherOr(s"a StaticSplice[${Format.TypeOf[T]}] cannot be summoned")
          // Summoning StaticSplice[T] will given (SpliceString: StaticSplice[String])
          // (a.k.a. Typed(Ident(SpliceString), TypeTree(StaticSplice[String])) usually with an outer inline surrounding it all)
          // so then we need to use Untype to just get SpliceString which is a module that we can load
          staticSpliceType = Untype(summonValue.asTerm.underlyingArgument).tpe.widen

          module <- {
            println(s"<<<<<<<<<<<<<<<< Just as term: ${Printer.TreeStructure.show(summonValue.asTerm)}")
            println(s"<<<<<<<<<<<<<<<< Summoned the actual value ${Format.Expr(summonValue)}")
            println(s"<<<<<<<<<<<<<<<< With the type ${Format.TypeRepr(staticSpliceType)}")
            println(s"<<<<<<<<<<<<<<<< named ${staticSpliceType.typeSymbol.fullName}")
            LoadModule.TypeRepr(staticSpliceType).toEither.mapLeft(_.getMessage)
          }

        } yield (module)

      out match
        case Right(value) =>
          println(s"<<<<<<<<<<<<<<<<<<< Summoned the actual module: ${value.getClass}")
        case Left(e) =>


      out.map(_.asInstanceOf[StaticSplice[T]])

  object SpliceString extends StaticSplice[String]:
    def apply(value: String) = s"'${value}'"
  inline given StaticSplice[String] = SpliceString

  object SpliceInt extends StaticSplice[Int]:
    def apply(value: Int) = s"${value}"
  given StaticSplice[Int] = SpliceInt

  object SpliceShort extends StaticSplice[Short]:
    def apply(value: Short) = s"${value}"
  given StaticSplice[Short] = SpliceShort

  object SpliceLong extends StaticSplice[Long]:
    def apply(value: Long) = s"${value}"
  given StaticSplice[Long] = SpliceLong

  object SpliceFloat extends StaticSplice[Float]:
    def apply(value: Float) = s"${value}"
  given StaticSplice[Float] = SpliceFloat

  object SpliceDouble extends StaticSplice[Double]:
    def apply(value: Double) = s"${value}"
  given StaticSplice[Double] = SpliceDouble

  object SpliceDate extends StaticSplice[java.sql.Date]:
    def apply(value: java.sql.Date) =
      value.toLocalDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  given StaticSplice[java.sql.Date] = SpliceDate

  object SpliceLocalDate extends StaticSplice[java.time.LocalDate]:
    def apply(value: java.time.LocalDate) =
      value.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  given StaticSplice[java.time.LocalDate] = SpliceLocalDate


end StaticSplice