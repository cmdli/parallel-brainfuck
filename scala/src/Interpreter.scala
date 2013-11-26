import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Phaser
import scala.actors.Actor

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(program: Program) {
    // Should be big enough
    val sizeOfData =  1000000
    
    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

    var programOps: List[List[Operation]] = program.calculateOperations()

    var threadsPerLine: Array[Int] = Array.fill[Int](programOps.size)(0)

    var pipeHandlers: Array[Phaser] = {
      var most = 0 // columns
      for (lineOps: List[Operation] <- programOps) {
        most = math.max(lineOps.size,most)
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
        Controller ! Start(0, sizeOfData / 2)
        Controller !? Wait
        Controller ! Finish
    }

    case class Stop(p: Process, line: Int)
    case class Start(line: Int, dataPointer: Int)
    case object Wait
    case object Finish

    object Controller extends Actor {
        var numThreads: Int = 0;

        def act {
          loop {
            react {
              case Start(line, dataPointer) => {
                if(line >= programOps.length)
                    println("Error in fork: starting line " + line + " when max line is " + (programOps.length-1))
                val process: Process = new Process(programOps, line, dataPointer, this)
                numThreads += 1
                threadsPerLine(line) += 1
                process.start()
                reply {true}
              }
              case Stop(process, line) => {
                numThreads -= 1
                threadsPerLine(line) -= 1
              }
              case Wait if (numThreads == 0) => reply { true }
              case Finish => exit()
            }
          }
        }

        this.start()
    }

    class Process(programOps: List[List[Operation]], line: Int, var dataPointer: Int, controller: Actor) extends Actor {

        def act = run()

        var pc = 0

        val lineOps = programOps(line)

        def run() {
            registerPipes()

            val maxPC: Int = lineOps.length

            while (pc < maxPC) {
                runOp()
                pc += 1
            }

            deregisterPipes()
            controller ! Stop(this, line)
            exit()
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
            // TODO: Make sure this is blocking
            controller !? Start(line + dataArr(dataPointer).get, dataPointer)
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
