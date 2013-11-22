/**
 * Parses a program in BK
 */
import scala.util.parsing.combinator._

abstract class Operation()
case class AddOperation() extends Operation()
case class SubOperation() extends Operation()
case class PrintOperation() extends Operation()
case class InputOperation() extends Operation()
case class ShiftRightOperation() extends Operation()
case class ShiftLeftOperation() extends Operation()
case class LoopOperations(ops: List[Operation]) extends Operation() {
    def operations = ops
}
case class ForkOperation() extends Operation()
case class InvalidOperation() extends Operation()

class Parser extends RegexParsers {

    //Parse a program string
    def parse(code: String) = parseAll(program, code)

    def program: Parser[List[List[Operation]]] = rep(line)

    def line: Parser[List[Operation]] = (block <~ "\n".?) ^^ {
        case b => b
    }

    //Block of code
    def block: Parser[List[Operation]] = rep(loop | char)

    //A single char
    def char: Parser[Operation] = "[^\\[\\]]".r ^^ {
        case "+" => new AddOperation()
        case "-" => new SubOperation()
        case "." => new PrintOperation()
        case "," => new InputOperation()
        case ">" => new ShiftRightOperation()
        case "<" => new ShiftLeftOperation()
        case "f" => new ForkOperation()
        case _ => new InvalidOperation()
    }

    //A loop in the code
    //Parsed by parsing the code inside the loop
    def loop: Parser[Operation] = ("[" ~> block <~ "]") ^^ {
        case operations => new LoopOperations(operations)
    }
}
