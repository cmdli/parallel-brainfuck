import java.util.concurrent.atomic.AtomicInteger
import scala.actors.Actor

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter {

    // Should be big enough
    val sizeOfData =  1000000

    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

    // Program starts in the middle of the "infinite" array
    var dataPointer = sizeOfData/2

    def runProgram(program: List[List[Operation]]): Array[AtomicInteger] = {
        var t = new Thread(new Process(program, 0))
        t.start()
        t.join()
        dataArr
}

    def runLine(line: String) {
        var index = 0
        while(index < line.length){
            line(index) match {
            case "+" => dataArr(dataPointer).incrementAndGet
            case "-" => dataArr(dataPointer).decrementAndGet
            case "." => print(dataArr(dataPointer).get.toChar)
            case "," => dataArr(dataPointer).set(Console.in.read)
            case ">" => dataPointer += 1
            case "<" => dataPointer -= 1
            case "[" => if(dataArr(dataPointer) == 0) index = matchEnd(line, index)
            case "]" => if(dataArr(dataPointer) != 0) index = matchStart(line, index)
            }
            index += 1
        }
    }

    //Run one operation
    def runOp(op: Operation): Unit = op match {
        case AddOperation() => add()
        case SubOperation() => subtract()
        case PrintOperation() => printData()
        case InputOperation() => scan()
        case ShiftRightOperation() => shiftRight()
        case ShiftLeftOperation() => shiftLeft()
        case s:StartLoopOperation => startLoop(s)
        case e:EndLoopOperation => endLoop(e)
        case ForkOperation() => fork()
        case InvalidOperation() => ()
    }

    def matchEnd(line: String, from:Int):Int {
        var index:Int = from
        var num:Int = 0
        while(index < line.length) {
            if(line.charAt(index) == '[')
                num += 1
            if(line.charAt(index) == ']') {
                if(num == 0)
                    return index
                num -= 1
            }
            index += 1
        }
        return -1
    }

    def matchStart(line: String, from:Int):Int {
        var index:Int = from
        var num:Int = 0
        while(index >= 0) {
            if(line.charAt(index) == ']')
                num += 1
            if(line.charAt(index) == '[') {
                if(num == 0)
                    return index
                num -= 1
            }
            index -= 1
        }
        return -1
    }

    def add() = dataArr(dataPointer).incrementAndGet

    def subtract() = dataArr(dataPointer).decrementAndGet

    def printData() = print(dataArr(dataPointer).get.toChar)

    def scan() = dataArr(dataPointer).set(Console.in.read)

    def shiftRight() = dataPointer += 1

    def shiftLeft() = dataPointer -= 1

    //Run a loop by running the code inside of it while data is zero
    /*def loop(loopOperations: List[Operation]) = {
        while (dataArr(dataPointer).get != 0) {
            for (op: Operation <- loopOperations) {
                runOp(op)
            }
        }
    }*/





}

