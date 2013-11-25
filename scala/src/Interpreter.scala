import java.util.concurrent.atomic.AtomicBoolean
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

    class Block(rows:mutable.LinkedList[Int]) {
        val lock:Lock = new Lock
        var count:Int = 0
        var exit = new AtomicBoolean(false)
        def checkIn() {
            lock.acquire
            if (rows.size == 0) {
                println("PANIC: pipe fault")
            }
            val mine = exit
            count += 1
            var current = 0
            for (row <- rows) {
                current += threads(row).size
            }
            if (current == count) {
                exit = new AtomicBoolean(false)
                mine.set(true)
            }
            lock.release
            while (!mine.get) {
                Thread.`yield`
            }
        }
    }

    val blockArr = {
        var most = 0 // columns
        for (lineOps: List[Operation] <- programOps) {
            most = math.max(lineOps.size,most)
        }
        val array:mutable.ArraySeq[Block] = new mutable.ArraySeq[Block](most)
        for (i <- 0 to (most - 1)) {
            array(i) = new Block(blockMap.getOrElse(i,new mutable.LinkedList[Int]))
        }
        array
    }

    val threads: Array[mutable.LinkedList[Thread]] = Array.fill[mutable.LinkedList[Thread]](programOps.length)(new mutable.LinkedList[Thread])

    val threadLocks: Array[Lock] = Array.fill[Lock](blockArr.size)(new Lock())

    def runProgram(): Array[AtomicInteger] = {
        globalFork(0, sizeOfData / 2)
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
        threadLocks(line).acquire()
        threads.update(line, t +: threads(line))
        threadLocks(line).release()
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
            blockArr(pc).checkIn
        }
    }
}
