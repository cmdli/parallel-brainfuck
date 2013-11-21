/**
 * Parses a program in BK
 */
import scala.util.parsing.combinator._

abstract class Operation()
case class AddOperation() extends Operation()
case class SubOperation() extends Operation()
case class PrintOperation() extends Operation()
case class ShiftRightOperation() extends Operation()
case class ShiftLeftOperation() extends Operation()
case class LoopOperations(ops: List[Operation]) extends Operation() {
  def operations = ops
}
case class UnknownOperation() extends Operation()

class Parser extends RegexParsers {
     def parse(code: String) = parseAll(parser(), code)

     def parser(): Parser[List[Operation]] = rep(parseLoop | char)

     def char: Parser[Operation] = ("+" | "-" | "." | ">" | "<") ^^ {
       case "+" => new AddOperation()
       case "-" => new SubOperation()
       case "." => new PrintOperation()
       case ">" => new ShiftRightOperation()
       case "<" => new ShiftLeftOperation()
     }

     def parseLoop(): Parser[Operation] = ("[" ~ rep(char) ~ "]") ^^ {
       case "[" ~ operations ~ "]" => new LoopOperations(operations)
     }
}
