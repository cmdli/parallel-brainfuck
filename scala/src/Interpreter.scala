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

    @volatile var globalTime:Int = 0

    def runProgram(program: List[List[Operation]]): Array[AtomicInteger] = {
        val t = new Thread(new Process(program, 0))
        t.start()
        t.join()
        dataArr
    }

    class Process(program: List[List[Operation]], line: Int) extends Runnable {

        var index:Int = 0
        var children: List[Thread] = null
        var localTime:Int = globalTime

        def pause() {
            var i:Int = globalTime
            while(localTime == i)
                i = globalTime
        }

        def run() {
            while(index < program.length) {
                pause()
                runOp(program(line)(index))
                index += 1
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
            case StartLoopOperation(i:Int) => maybeJumpForward(i)
            case EndLoopOperation(i:Int) => maybeJumpBack(i)
            case ForkOperation() => fork()
            case InvalidOperation() => ()
        }

        def add() = dataArr(dataPointer).incrementAndGet

        def subtract() = dataArr(dataPointer).decrementAndGet

        def printData() = print(dataArr(dataPointer).get.toChar)

        def scan() = dataArr(dataPointer).set(Console.in.read)

        def shiftRight() = dataPointer += 1

        def shiftLeft() = dataPointer -= 1

        def maybeJumpBack(jump:Int) = if(dataArr(dataPointer).get != 0) index -= jump

        def maybeJumpForward(jump:Int) = if(dataArr(dataPointer).get == 0) index += jump

        //Run a loop by running the code inside of it while data is zero
        def loop(loopOperations: List[Operation]) = {
            while (dataArr(dataPointer).get != 0) {
                for (op: Operation <- loopOperations) {
                    runOp(op)
                }
            }
        }

        def fork() {
            var t = new Thread(new Process(program, index+1))
            t.start()
            children = t :: children
        }
    }





}

