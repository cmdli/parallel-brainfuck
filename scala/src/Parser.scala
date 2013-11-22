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
case class StartLoopOperation(jump:Int) extends Operation()
case class EndLoopOperation(jump:Int) extends Operation()
case class ForkOperation() extends Operation()
case class InvalidOperation() extends Operation()

class Parser extends RegexParsers {

    //Parse a program string
    def parse(code: String) = parseAll(program, code)

    def program: Parser[List[List[Operation]]] = {
        rep(line)
    }

    def line: Parser[List[Operation]] = {
        println("Parsing line...")
        (block ~ "\n".?) ^^ {
            case b:(~[List[Operation],Option[String]]) => b._1
        }
    }

    //Block of code
    def block: Parser[List[Operation]] =  (loop.* | char.*) ^^ {
        case l:List[List[Operation]] => l.flatten
        case c:List[Operation] => c
    }

    //A single char
    def char: Parser[Operation] = ("+" | "-" | "." | "," | ">" | "<" | "f") ^^ {
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
    def loop: Parser[List[Operation]] = "[" ~ block ~ "]" ^^ {
        case "[" ~ operations ~ "]" => {
            val ops = operations.asInstanceOf[List[Operation]]
            val start = new StartLoopOperation(ops.length+1)
            val end = new EndLoopOperation(ops.length+1)
            (start :: ops) :+ end
        }
    }
}
