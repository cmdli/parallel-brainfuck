/**
 * Parses a program in BK
 */
import scala.util.parsing.combinator._

abstract class Operation
case class AddOperation() extends Operation
case class UnknownOperation() extends Operation

class Parser extends RegexParsers {
     def parse(code: String) = parseAll(parser(), code)

     def parser(): Parser[List[Operation]] = rep(parseAdd())
     def parseAdd(): Parser[Operation] = "+" ^^ {
       case "+" => new AddOperation()
     }
}
