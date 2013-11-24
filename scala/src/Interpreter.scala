import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Lock
import scala.collection.mutable.LinkedList
import scala.collection.mutable.MutableList
import scala.collection.mutable.ArraySeq
import scala.collection.immutable.HashMap

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(program: List[List[Operation]]) {
    // Should be big enough
    val sizeOfData =  1000000
    
    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))
    
    // for pipe operator
    // column -> line numbers
    var blockMap:Map[Int,LinkedList[Int]] = new HashMap[Int,LinkedList[Int]]
    
    var blockArr = {
        var most = 0
        for (lineOps: List[Operation] <- program) {
            most = math.max(lineOps.size,most)
        }
        Array.fill[AtomicInteger](most)(new AtomicInteger(0))
    }

    var threads:ArraySeq[LinkedList[Thread]] = new ArraySeq[LinkedList[Thread]](program.length)

    var threadLock:Lock = new Lock()

    def runProgram(): Array[AtomicInteger] = {
        var first = new Thread(new Process(program, 0, sizeOfData / 2))
        program.indices.foreach(i => init(i, ops=program(i)))
        first.start()
        first.join()
        for(lineThreads:LinkedList[Thread] <- threads) {
            if (lineThreads != null) {
                for(t:Thread <- lineThreads) {
                    t.join()
                }
            }
        }
        dataArr
    }
    
    // call this before running the code on all lines
    def init(line_number:Int = 0, pc_val:Int = 0, ops:List[Operation]):Int = {
        var pc_temp:Int = pc_val
        for (op <- ops) {
            if (op.isInstanceOf[PipeOperation]) {
                // blockArr(pc_temp).incrementAndGet
                // 
                var set:LinkedList[Int] = blockMap get pc_temp match {
                    case Some(b) => b
                    case None => new LinkedList[Int]
                }
                blockMap = blockMap.updated(pc_temp, line_number +: set)
            } else {
                if (op.isInstanceOf[LoopOperations]) {
                    // loops are entered in the loop
                    pc_temp += 1 // [
                    pc_temp += init(line_number, pc_temp,op.asInstanceOf[LoopOperations].ops)
                    // ] is done below
                }
            }
            pc_temp += 1
        }
        pc_temp
    }

    def globalFork(line:Int, dataPointer:Int) {
        val t = new Thread(new Process(program, line, dataPointer))
        threadLock.acquire()
        if(threads(line) == null)
            threads(line) = new LinkedList[Thread]()
        threads.update(line, t +: threads(line))
        threadLock.release()
        t.start
    }

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
            case PipeOperation() => pipe()
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
            pc += 1 // [
            while (dataArr(dataPointer).get != 0) {
                if (loopOperations.size != 0) {
                    for (op: Operation <- loopOperations) {
                        runOp(op)
                        pc += 1
                    }
                } else {
                    Thread.`yield`
                }
                pc -= loopOperations.size
            }
            pc += loopOperations.size
        }

        def fork() {
            globalFork(line + dataArr(dataPointer).get, dataPointer)
        }

        def pipe() {
            // TODO
            while (blockArr(pc).get != 0) {
                Thread.`yield`
            }
        }
    }
}
