/**
 * Parses a program in BK
 */
import scala.util.parsing.combinator._
import scala.collection.immutable.LinearSeq

abstract class Operation()
case class AddOperation() extends Operation()
case class SubOperation() extends Operation()
case class PrintOperation() extends Operation()
case class InputOperation() extends Operation()
case class ShiftRightOperation() extends Operation()
case class ShiftLeftOperation() extends Operation()
case class LoopOperations() extends Operation() {
    var pair:LoopOperations = null
    def set(newPair: LoopOperations) {
        pair = newPair
    }
}
case class StartLoopOperation() extends LoopOperations()
case class EndLoopOperation() extends LoopOperations()
case class ForkOperation() extends Operation()
case class InvalidOperation() extends Operation()

class Parser extends RegexParsers {

    //Parse a program string
    def parse(code: String) = parseAll(program, code)

    def program: Parser[List[List[Operation]]] = rep(line)

    def line: Parser[List[Operation]] = (block ~ "\n".?) ^^ {
        case b:(~[List[Operation],Option[String]]) => b._1
    }

    //Block of code
    def block: Parser[List[Operation]] = {
        println("Parsing block...")
        (loop | char).*
    }

    //A single char
    def char: Parser[Operation] = ("+" | "-" | "." | "," | ">" | "<" | "f") ^^ {
        case "+" => new AddOperation()
        case "-" => new SubOperation()
        case "." =>new PrintOperation()
        case "," => new InputOperation()
        case ">" =>new ShiftRightOperation()
        case "<" => new ShiftLeftOperation()
        case "f" => new ForkOperation()
        case _ => new InvalidOperation()
    }

    //A loop in the code
    //Parsed by parsing the code inside the loop
    def loop: Parser[Operation] =  "[" ~ block ~ "]" ^^ {
        case l:(~[String,~[List[Operation],String]]) => {
            val start = StartLoopOperation()
            val end = EndLoopOperation()
            start.set(end)
            end.set(start)
            start :: l._2._1 :: end
        }
    }
}
