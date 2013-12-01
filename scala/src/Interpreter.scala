import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Phaser
import scala.actors.Actor
import scala.collection.mutable

/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter(programOps: List[List[Operation]], debugging: Boolean) {
    // Should be big enough
    val sizeOfData =  1000000
    
    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

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
    case class CanIRun(line: Int)
    case object LetAnyoneRun
    case class LetThisRun(line: Int)
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
                val process: Process = new Process(programOps, line, dataPointer)
                numThreads += 1
                process.registerPipes()
                process.start()
                reply {true}
              }

              // *** START JUST FOR DEBUGGING *** //
              case CanIRun(lineOfRequester) if ((lineOfRequester == processLine) || anyoneCanRun) => anyoneCanRun = false; reply{true}
              case LetThisRun(lineToRun) => processLine = lineToRun
              case LetAnyoneRun => anyoneCanRun = true
              // *** END JUST FOR DEBUGGING *** //

              case Stop(process, line) => {
                numThreads -= 1
                process.deregisterPipes()
              }
              case Wait if (numThreads == 0) => reply { true }
              case Finish => exit()
            }
          }
        }

        this.start()
    }

    var processLine = 0
    var anyoneCanRun = false
    var stepping: Boolean = debugging
    // Line -> breakpoints in each line
    var breakpoints: mutable.HashMap[Int, mutable.HashSet[Int]] = new mutable.HashMap[Int, mutable.HashSet[Int]]


  class Process(programOps: List[List[Operation]], line: Int, var dataPointer: Int) extends Actor {

        def act = run()

        var pc = 0

        val lineOps = programOps(line)


        def run() {
            val maxPC: Int = lineOps.length

            while (pc < maxPC) {
                if (debugging) {
                  Controller !? CanIRun(line)

                  if (stepping || isBreakpointAtPC()) {
                    debuggingStepper()
                  }
                }
                runOp()
                pc += 1
            }

            // deregisterPipes()
            Controller ! Stop(this, line)
            exit()
        }

      // *** START JUST FOR DEBUGGING *** //
      def isBreakpointAtPC(): Boolean = {
          var breakpointAtPC = false
          breakpoints.get(line) match {
            case Some(set: mutable.HashSet[Int]) => if (set contains pc) breakpointAtPC = true
            case None => ()
          }
          breakpointAtPC
        }

        def debuggingStepper() {
            println("\nCurrent instruction: " + lineOps(pc) + " at pc: " + pc + " in line: " + line)
            println("What do you want to do? (h for help)")

            var continueCommandEntered: Boolean = false
            while (!continueCommandEntered) {

              val BreakpointPattern1 = "(b\\s+)([0-9]+)".r
              val BreakpointPattern2 = "(b\\s+)([0-9]+)(\\s+)([0-9]+)".r
              val SwitchPattern = "(switch\\s+)([0-9]+)".r
              val DataPattern = "(data\\s+)([0-9]+)".r

              print("> ")
              val command: String = readLine()
              command match {
                case "h" => helpCommands(); continueCommandEntered = false
                case "c" => stepping = false; continueCommandEntered = true
                case "s" => stepping = true; continueCommandEntered = true
                case BreakpointPattern1(_, pcNumber) => {

                  breakpoints.get(line) match {
                    case Some(set: mutable.HashSet[Int]) => set += pcNumber.toInt
                    case None => breakpoints += ((line, new mutable.HashSet[Int]() += pcNumber.toInt))
                  }

                  println("Breakpoint registered at pc: " + pcNumber)
                  continueCommandEntered = false
                }
                case BreakpointPattern2(_, pcNumber, _, lineNumber) => {

                  breakpoints.get(lineNumber.toInt) match {
                    case Some(set: mutable.HashSet[Int]) => set += pcNumber.toInt
                    case None => breakpoints += ((lineNumber.toInt, new mutable.HashSet[Int]() += pcNumber.toInt))
                  }

                  println("Breakpoint registered at pc: " + pcNumber + " and line: " + lineNumber)
                  continueCommandEntered = false
                }
                case SwitchPattern(_, lineNumber) => {
                  stepping = true
                  Controller ! LetThisRun(lineNumber.toInt)
                  Controller !? CanIRun(line)
                  continueCommandEntered = true
                }
                case DataPattern(_, position) => {
                  println("Data at position: " + position + " is: " + dataArr(position.toInt + dataPointer))
                  continueCommandEntered = false
                }
                case _ => println("Invalid command. Press h to see all commands."); continueCommandEntered = false
              }
            }
        }

        def helpCommands() {
            printf("%-30s%s\n", "s", "step one instruction")
            printf("%-30s%s\n", "c", "continue execution until breakpoint or completion")
            printf("%-30s%s\n", "b {pc}", "set breakpoint at specified program counter within the current line")
            printf("%-30s%s\n", "b {pc} {line}", "set breakpoint at specified program counter within the specified line")
            printf("%-30s%s\n", "switch {line}", "change debugger to start executing specified line")
            printf("%-30s%s\n", "data {position}", "prints the value of the data at the specified offset from the current data pointer")

          println()
        }
      // *** END JUST FOR DEBUGGING *** //


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
