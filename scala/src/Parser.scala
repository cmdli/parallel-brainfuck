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
case class StartLoopOperation(var endPC:Int) extends Operation() {
  def end = endPC
}
case class EndLoopOperation(var startPC:Int) extends Operation() {
  def start = startPC
}
case class ForkOperation() extends Operation()
case class InvalidOperation() extends Operation()
case class PipeOperation() extends Operation()

class Parser extends RegexParsers {

    var pc = 0

    override def skipWhitespace = false
    
    //Parse a program string
    def parse(code: String) = parseAll(program, code)

    def program: Parser[List[List[Operation]]] = rep(line|block)

    def line: Parser[List[Operation]] = (block <~ "\n") ^^ {
        case b => pc = 0; b
    }

    //Block of code
    def block: Parser[List[Operation]] = rep1(char)

    //A single char
    def char: Parser[Operation] = "[^\\n]".r ^^ {
        case "+" => new AddOperation()
        case "-" => new SubOperation()
        case "." => new PrintOperation()
        case "," => new InputOperation()
        case ">" => new ShiftRightOperation()
        case "<" => new ShiftLeftOperation()
        case "*" => new ForkOperation()
        case "|" => new PipeOperation()
        // The start and end values of loops
        // will be corrected in Program.finalizeProgram
        case "[" => new StartLoopOperation(-1)
        case "]" => new EndLoopOperation(-1)
        case _ => new InvalidOperation()
    }
}
