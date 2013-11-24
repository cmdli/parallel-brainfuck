/**
 * Parses a program in BK
 */

import scala.util.parsing.combinator._
import scala.collection.immutable.HashMap
import scala.collection.mutable.{ArrayBuffer, LinkedList}

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
case class PipeOperation() extends Operation() {
    var list:List[Int] = null
}

class Parser extends RegexParsers {

    var pc = 0

    override def skipWhitespace = false
    
    //Parse a program string
    def parse(code: String) = {
        val parsed = parseAll(program, code)
        //fixPipes(parsed.get)
        parsed
    }

    def fixPipes(program:List[List[Operation]]) {
        var map:Map[Int, ArrayBuffer[Int]] = new HashMap[Int, ArrayBuffer[Int]]
        var line:Int = 0
        while(line < program.length) {
            var pc:Int = 0
            while(pc < program(line).length) {
                if(program(line)(pc).isInstanceOf[PipeOperation]) {
                    var list:ArrayBuffer[Int] = map.getOrElse(pc, new ArrayBuffer[Int])
                    list += line
                }
                pc += 1
            }
            line += 1
        }

        for(pair <- map) {
            var pc = pair._1
            var list:ArrayBuffer[Int] = pair._2
            val finalList = list.toList
            for(line <- list)
                program(line)(pc).asInstanceOf[PipeOperation].list = finalList
        }
    }

    def program: Parser[List[List[Operation]]] = rep(line|block)

    def line: Parser[List[Operation]] = (block <~ "\n") ^^ {
        case b => pc = 0; b
    }

    //Block of code
    def block: Parser[List[Operation]] = rep1(loop | rep1(char)) ^^{
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
        // The start and end values of loops
        // will be corrected in Program.finalizeProgram
        case "[" => new StartLoopOperation(-1)
        case "]" => new EndLoopOperation(-1)
        case _ => new InvalidOperation()
    }

    def loop: Parser[List[Operation]] = "[" ~> block <~ "]" ^^  {
        case ops:List[Operation] =>
            (StartLoopOperation(ops.length+1) +: ops) :+ EndLoopOperation(ops.length+1)
    }
}
