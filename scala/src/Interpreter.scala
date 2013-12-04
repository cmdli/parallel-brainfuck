import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Phaser
import scala.actors.Actor
import scala.collection.mutable
import scala.collection.mutable.LinkedList
import scala.collection.mutable.HashSet
import scala.util.Sorting.quickSort

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(programOps: List[List[Operation]]) {
    // Should be big enough
    val sizeOfData = 1000000

    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData){new AtomicInteger(0)}

    //List of currently existing threads (used in debugging)
    var threads:Array[LinkedList[Process]] = null

    var pipeHandlers: Array[Phaser] = {
        var most = 0 // columns
        for (lineOps: List[Operation] <- programOps) {
            most = math.max(lineOps.size, most)
        }
        val array: Array[Phaser] = new Array[Phaser](most)
        for (i <- 0 to (most - 1)) {
            array(i) = new Phaser() {
                override def onAdvance(phase: Int, registeredParties: Int): Boolean = false
            }
        }
        array
    }

    def runProgram(): Unit = {
        if(debug) {
            threads = new Array[mutable.LinkedList[Process]](programOps.length)
            for(i <- 0 to threads.length - 1)
                threads(i) = new LinkedList[Process]
        }
        Controller ! Start(0, sizeOfData / 2)
        Controller !? Wait
        Controller ! Finish
    }

    var debug:Boolean = false
    def startProgram() {
        threads = Array.fill[LinkedList[Process]](programOps.length){new LinkedList[Process]()}
        Controller ! Start(0, sizeOfData/2)
    }
    def stopProgram() = Controller ! Finish
    def getData(index: Int): Int = dataArr(index+sizeOfData/2).get()
    def getThreads() = threads
    def step(line:Int) {
        if(line >= threads.length) {
            println("Line index out of bounds!")
            return
        }
        for(t <- threads(line))
            t.step()
    }
    def step(line:Int, thread:Int) {
        if(line >= threads.length) {
            println("Line index out of bounds!")
            return
        }
        if(thread >= threads(line).length) {
            println("Thread index out of bounds!")
            return
        }
        threads(line)(thread).step()
    }
    def continue() {
        var breakpointReached = false
        while(getNumThreads() > 0 && !breakpointReached) {
            var line = 0
            while(line < threads.length) {
                val lineT = threads(line)
                for(t <- lineT) {
                    t.step()
                    if(breakpoints.get(line) match {
                        case Some(set: HashSet[Int]) => set.contains(t.pc)
                        case None => false})
                        breakpointReached = true
                }
                line += 1
            }
        }
    }
    def stepAll() {
        for(lineT <- threads)
            for(t <- lineT)
                t.step()
    }
    def atPipe(line:Int, thread:Int):Boolean = {
        println("line: " + line + " thread: " + thread)
        if(line < threads.length && thread < threads(line).length)
            threads(line)(thread).awaitingPhaser != -1
        false
    }
    def addBreakpoint(pc:Int, line:Int) = Controller !? Breakpoint(pc,line)
    def getNumThreads():Int = (Controller !? NumThreads).asInstanceOf[Int]
    def getPCs(line:Int):Array[(Int,Int)] = {
        if(debug) {
            threads(line) match {
                case t:LinkedList[Process] => {
                    var b:Array[(Int,Int)] = new Array[(Int,Int)](t.size)
                    for(i <- 0 to (b.length - 1)) {
                        println(i)
                        b(i) = (t(i).pc,i)
                    }
                    quickSort(b)
                    b
                }
                case null => new Array[(Int,Int)](0)
            }
        }
        else
            new Array[(Int,Int)](0)
    }

    case class Stop(p: Process, line: Int)
    case class Start(line: Int, dataPointer: Int)
    case object Wait
    case object Finish
    case class Step(line:Int)
    case object StepAll
    case object Continue
    case class Breakpoint(pc:Int,line:Int)
    case object NumThreads

    object Controller extends Actor {
        @volatile var numThreads: Int = 0

        def act() {
            loop {
                react {
                    case Start(line, dataPointer) => {
                        if (line >= programOps.length)
                            println("Error in fork: starting line " + line + " when max line is " + (programOps.length - 1))
                        val process: Process = new Process(programOps, line, dataPointer)
                        numThreads += 1
                        process.registerPipes()
                        if(debug)
                            threads(line) = threads(line) :+ process
                        else
                            process.start()
                        reply {true}
                    }

                    // *** START JUST FOR DEBUGGING *** //
                    case Breakpoint(pc:Int, line:Int) => breakpoints.get(line) match {
                        case Some(set: HashSet[Int]) => set += pc
                        case None => breakpoints += ((line, new mutable.HashSet[Int]() += pc))
                    }; reply{true}
                    case NumThreads => reply{numThreads}
                    // *** END JUST FOR DEBUGGING *** //

                    case Stop(process, line) => {
                        numThreads -= 1
                        process.deregisterPipes()
                        if(debug) { //Remove the process from the list of threads
                            val (left,right) = threads(line).span(_ != process)
                            threads(line) = left.append(right.tail)
                        }
                        reply{true}
                    }
                    case Wait if (numThreads == 0) => reply {true}
                    case Finish => exit()
                }
            }
        }

        this.start()
    }

    // Line -> breakpoints in each line
    var breakpoints: mutable.HashMap[Int, mutable.HashSet[Int]] = new mutable.HashMap[Int, mutable.HashSet[Int]]

  /**
   * A thread of the program.
   * @param programOps All the code for all lines.
   * @param line Which line of code the process is running.
   * @param dataPointer
   * @param instance Which process of the type "line" is it. Only used in debugging.
   */
    class Process(programOps: List[List[Operation]], line: Int, var dataPointer: Int, instance: Int = -1) extends Actor {

        def act() = run()

        var pc = 0

        val lineOps = programOps(line)

        def run() {
            val maxPC: Int = lineOps.length

            while (pc < maxPC) {
                runOp()
                pc += 1
            }

            Controller ! Stop(this, line)
            exit()
        }

        var awaitingPhaser = -1
        def step() = {
            if(awaitingPhaser != -1) {
                if(pipeHandlers(pc).getPhase != awaitingPhaser) {
                    awaitingPhaser = -1
                    pc += 1
                }
            }
            else {
                if(pc < lineOps.length) {
                    lineOps(pc) match {
                    case PipeOperation() => {
                        awaitingPhaser = pipeHandlers(pc).getPhase
                        pipeHandlers(pc).arrive()
                        //if(pipeHandlers(pc).getPhase != awaitingPhaser)
                        //    awaitingPhaser = -1
                        //else
                            pc -= 1
                    }
                    case _ => runOp()
                    }
                    pc += 1
                }
                else {
                    Controller !? Stop(this,line)
                }
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
            Controller !? Start(line + dataArr(dataPointer).get, dataPointer)
        }

        def pipe() {
            pipeHandlers(pc).arriveAndAwaitAdvance()
        }

        def deregisterPipes() = {
            for (i <- 0 to lineOps.size - 1) {
                lineOps(i) match {
                    case PipeOperation() => pipeHandlers(i).arriveAndDeregister()
                    case _ => ()
                }
            }
        }

        def registerPipes() = {
            for (i <- 0 to lineOps.size - 1) {
                lineOps(i) match {
                    case PipeOperation() => pipeHandlers(i).register()
                    case _ => ()
                }
            }
        }
    }

}
