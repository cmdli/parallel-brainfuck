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
case class StartLoopOperation(var numLoopOps: Int) extends Operation()
case class EndLoopOperation(var numLoopOps: Int) extends Operation()
case class ForkOperation() extends Operation()
case class InvalidOperation() extends Operation()
case class PipeOperation() extends Operation()

class Parser extends RegexParsers {

    override def skipWhitespace = false
    
    //Parse a program string
    def parse(code: String) = parseAll(program, code)

    def program: Parser[List[List[Operation]]] = rep(line|block)

    def line: Parser[List[Operation]] = (block <~ "\n") ^^ {
        case b => b
    }

    //Block of code
    def block: Parser[List[Operation]] = rep1(loop | rep1(char)) ^^ {
        case b:List[List[Operation]] => b.flatten
    }


    //A single char
    def char: Parser[Operation] = "[^\\[\\]\\n]".r ^^ {
        case "+" => new AddOperation()
        case "-" => new SubOperation()
        case "." => new PrintOperation()
        case "," => new InputOperation()
        case ">" => new ShiftRightOperation()
        case "<" => new ShiftLeftOperation()
        case "*" => new ForkOperation()
        case "|" => new PipeOperation()
        case _ => new InvalidOperation()
    }

    def loop: Parser[List[Operation]] = (("[" ~> block <~ "]") | "[]") ^^  {
      case ops:List[Operation] =>
        (StartLoopOperation(ops.length+1) +: ops) :+ EndLoopOperation(ops.length+1)
      case "[]" => List(StartLoopOperation(1), EndLoopOperation(1))
    }
}
