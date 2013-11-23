
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Lock
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ArraySeq

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(program: List[List[Operation]]) {

    // Should be big enough
    val sizeOfData =  1000000

    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

    var threads:ArraySeq[LinkedList[Thread]] = new ArraySeq[LinkedList[Thread]](program.length)

    var threadLock:Lock = new Lock()

    def runProgram(): Array[AtomicInteger] = {
        var first = new Thread(new Process(program, 0, sizeOfData / 2))
        first.start()
        first.join()
        for(lineThreads:LinkedList[Thread] <- threads) {
            for(t:Thread <- lineThreads) {
                t.join()
            }
        }
        dataArr
    }

    def globalFork(line:Int, dataPointer:Int) {
        threadLock.acquire()
        if(threads(line) == null)
            threads(line) = new LinkedList[Thread]()
        threads.update(line, new Thread(new Process(program, line, dataPointer)) +: threads(line))
        threads(line).head.start()
        threadLock.release()
    }

    // TODO check dataPointer copied by value
    class Process(program: List[List[Operation]], line: Int, var dataPointer: Int) extends Runnable {

        var pc = 0

        def run() {
            for (op: Operation <- program(line)) {
                runOp(op)
                pc += 1
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
                    pc += 1
                }
                pc -= loopOperations.size
            }
            pc += loopOperations.size
        }

        def fork() {
            globalFork(line + dataArr(dataPointer).get, dataPointer)
        }

        def pipe() {

        }
    }
}
