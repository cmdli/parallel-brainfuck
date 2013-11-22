
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

    def runProgram(program: List[List[Operation]]): Array[AtomicInteger] = {
        var t = new Thread(new Process(program, 0, sizeOfData / 2))
        t.start()
        t.join()
        dataArr
    }

    // TODO check dataPointer copied by value
    class Process(program: List[List[Operation]], index: Int, var dataPointer: Int) extends Runnable {
        var children: List[Thread] = null

        def run() {
            for (op: Operation <- program(index)) {
                runOp(op)
            }
            if(children != null) {
                for(child: Thread <- children)
                    child.join()
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
            case LoopOperations(operations) => loop(operations)
            case ForkOperation() => fork()
            case InvalidOperation() => ()
        }

        def add() = dataArr(dataPointer).incrementAndGet

        def subtract() = dataArr(dataPointer).decrementAndGet

        def printData() = print(dataArr(dataPointer).get.toChar)

        def scan() = dataArr(dataPointer).set(Console.in.read)

        def shiftRight() = dataPointer += 1

        def shiftLeft() = dataPointer -= 1

        //Run a loop by running the code inside of it while data is zero
        def loop(loopOperations: List[Operation]) = {
            while (dataArr(dataPointer).get != 0) {
                for (op: Operation <- loopOperations) {
                    runOp(op)
                }
            }
        }

        def fork() {
            var t = new Thread(new Process(program, index+1, dataPointer))
            t.start()
        }
    }





}
