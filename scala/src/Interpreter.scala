import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Lock
import scala.collection.mutable

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(program: Program) {
    // Should be big enough
    val sizeOfData =  1000000
    
    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

    // for pipe operator
    // column -> line numbers
    var blockMap: Map[Int, mutable.LinkedList[Int]] = program.calculateBlockMap()

    var programOps: List[List[Operation]] = program.calculateOperations()

    var blockArr = {
        var most = 0
        for (lineOps: List[Operation] <- programOps) {
            most = math.max(lineOps.size,most)
        }
        Array.fill[AtomicInteger](most)(new AtomicInteger(0))
    }

    var threads: mutable.ArraySeq[mutable.LinkedList[Thread]] = new mutable.ArraySeq[mutable.LinkedList[Thread]](programOps.length)

    var threadLock:Lock = new Lock()

    def runProgram(): Array[AtomicInteger] = {
        val first = new Thread(new Process(programOps, 0, sizeOfData / 2))
        first.start()
        first.join()
        for(lineThreads: mutable.LinkedList[Thread] <- threads) {
            if (lineThreads != null) {
                for(t:Thread <- lineThreads) {
                    t.join()
                }
            }
        }
        dataArr
    }

    def globalFork(line:Int, dataPointer:Int) {
        val t = new Thread(new Process(programOps, line, dataPointer))
        threadLock.acquire()
        if(threads(line) == null)
            threads(line) = new mutable.LinkedList[Thread]()
        threads.update(line, t +: threads(line))
        threadLock.release()
        t.start()
    }

    class Process(programOps: List[List[Operation]], line: Int, var dataPointer: Int) extends Runnable {

        var pc = 0

        val lineOps = programOps(line)

        def run() {
            val maxPC: Int = lineOps.length

            while (pc < maxPC) {
                runOp()
                pc += 1
            }
        }

        // Run one operation.
        def runOp(): Unit = lineOps(pc) match {
            case AddOperation() => add()
            case SubOperation() => subtract()
            case PrintOperation() => printData()
            case InputOperation() => scan()
            case ShiftRightOperation() => shiftRight()
            case ShiftLeftOperation() => shiftLeft()
            case StartLoopOperation(jump) => startLoop(jump)
            case EndLoopOperation(jump) => endLoop(jump)
            case ForkOperation() => fork()
            case PipeOperation() => pipe()
            case InvalidOperation() => ()
        }

        def add() = dataArr(dataPointer).incrementAndGet

        def subtract() = dataArr(dataPointer).decrementAndGet

        def printData() = print(dataArr(dataPointer).get.toChar)

        def scan() = dataArr(dataPointer).set(Console.in.read)

        def shiftRight() = dataPointer += 1

        def shiftLeft() = dataPointer -= 1

        // Jump to the matching end ] if the current data is 0.
        def startLoop(jump: Int) = {
          if (dataArr(dataPointer).get == 0) {
            pc += jump
          }
        }

        // Return to the matching start [ if the current data is not 0.
        def endLoop(jump: Int) {
          if (dataArr(dataPointer).get != 0) {
            pc -= jump
          }
        }

        def fork() {
            globalFork(line + dataArr(dataPointer).get, dataPointer)
        }

        def pipe() {
            // TODO
            while (blockArr(pc).get != 0)
            {
              Thread.`yield`()
            }
        }
    }
}
