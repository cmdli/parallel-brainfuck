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
case class LoopOperations(ops: List[Operation]) extends Operation() {
    def operations = ops
}
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
    def block: Parser[List[Operation]] = {
        println("Parsing block...")
        (loop | char).*
    }

    //A single char
    def char: Parser[Operation] = {
        println("Parsing character...")
        ("+" | "-" | "." | "," | ">" | "<" | "f") ^^ {
            case "+" => {
                println("Creating new +...")
                new AddOperation()
            }
            case "-" => {
                println("Creating new -...")
                new SubOperation()
                }
            case "." => {
                println("Creating new '.'...")
                new PrintOperation()
                    }
            case "," => {
                println("Creating new ','...")
                new InputOperation()
                        }
            case ">" => {
                println("Creating new >...")
                new ShiftRightOperation()
                            }
            case "<" => {
                println("Creating new <...")
                new ShiftLeftOperation()
                                }
            case "f" => {
                println("Creating new f...")
                new ForkOperation()
            }
            case _ => {
                println("Creating new InvalidOperation...")
                new InvalidOperation()
            }
        }
    }

    //A loop in the code
    //Parsed by parsing the code inside the loop
    def loop: Parser[Operation] = {
        println("Parsing loop...")
        "[" ~ block ~ "]" ^^ {
            case "[" ~ operations ~ "]" => {
                println("Creating new LoopOperations...")
                new LoopOperations(operations)
            }
        }
    }
}
