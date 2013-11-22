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
case class UnknownOperation(x:String) extends Operation()

class Parser extends RegexParsers {
    //Skip whitespace in the program
    override def skipWhitespace = true

    //Parse a program string
    def parse(code: String) = parseAll(block, code)

    //Block of code
    def block: Parser[List[Operation]] = rep(loop | char)

    //A single char
    def char: Parser[Operation] = ".".r ^^ {
        case "+" => new AddOperation()
        case "-" => new SubOperation()
        case "." => new PrintOperation()
        case "," => new InputOperation()
        case ">" => new ShiftRightOperation()
        case "<" => new ShiftLeftOperation()
        case u:String => new UnknownOperation(u)
    }

    //A loop in the code
    //Parsed by parsing the code inside the loop
    def loop: Parser[Operation] = ("[" ~ block ~ "]") ^^ {
        case "[" ~ operations ~ "]" => new LoopOperations(operations)
    }
}
