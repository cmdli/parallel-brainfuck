import scala.collection.mutable
import scala.collection.mutable.HashSet

/**
 * Provides a debugging interface to the interpreter
 */
class Debugger {

    val StepLinePattern = "s\\s+([0-9]+)".r
    val StepLineThreadPattern = "s\\s+([0-9]+)\\s+([0-9]+)".r
    val BreakpointPattern1 = "(b\\s+)([0-9]+)".r
    val BreakpointPattern2 = "b\\s+([0-9]+)\\s+([0-9]+)".r
    val SwitchPattern = "(switch\\s+)([0-9]+)".r
    val DataPattern = "(data\\s+)([0-9]+)".r

    def debug(program:Array[String], interpreter:Interpreter) {
        interpreter.debug = true
        interpreter.startProgram()
        while(interpreter.getNumThreads() > 0) {
            displayLines(program, interpreter)
            print("(h for help) > ")
            val command = readLine()
            println()
            control(command,program,interpreter)
        }
        interpreter.stopProgram()
    }


    def control(instruction:String, program:Array[String], interpreter:Interpreter) {
        instruction match {
            case StepLinePattern(line) => println("Stepping line " + line + "...");interpreter.step(line.toInt) //Step the specified line
            case StepLineThreadPattern(line, thread) => println("Stepping thread " + thread + " on line " + line + "..."); interpreter.step(line.toInt,thread.toInt)
            case "s" => println("Stepping all threads...");interpreter.stepAll()
            case "c" => println("Continuing...");interpreter.continue() //Continue until a thread hits a breakpoint
            //case BreakpointPattern1(_, pc) => interpreter.addBreakpoint(pc)
            case BreakpointPattern2(pc,line) => printf("Breakpoint at (%s,%s)\n", pc, line); interpreter.addBreakpoint(pc.toInt,line.toInt)
            case "h" => helpCommands()
            case "t" => displayLines(program,interpreter,useNums = true)
            case _ => ()
        }
    }

    def display(program:Array[String], interpreter:Interpreter) {

        var line = 0
        while(line < program.length) {
            val lineString = program(line)

            println("\nInstances of line " + line + ":")

            if (getNumProcessesOfLine(interpreter, line) == 0) {
              println("No Instances")
            }
            else {
              // Label instance information
              printf("\n%-5s %s", "id", "line code")
            }

            var instance = 0
            while (instance < getNumProcessesOfLine(interpreter, line)) {
              //TODO: Handle multiple threads per line (With a list)
              val pc:Int = getPC(interpreter, line, instance)
              if(pc < 0 || pc > lineString.length)
                printf("\n%-5s %s", instance + ":", lineString)
              else {
                printf("\n%-5s %s", instance + ":", lineString.substring(0,pc)
                                                    + Console.RED + lineString.substring(pc,pc+1)
                                                    + Console.RESET + lineString.substring(pc+1))
              }
              println()
              instance += 1
            }
            line += 1
        }
        println()

        //Display data
        for(x:Int <- -10 to 10) {
            printf("|%1$4d".format(x))
        }
        println()
        for(x:Int <- -10 to 10) {
            printf("|%1$4d".format(interpreter.getData(x)))
        }
        println()
    }

    def displayLines(program:Array[String], interpreter:Interpreter, useNums:Boolean = false) {

        var line = 0
        while(line < program.length) {
            val lineString:String = program(line)
            //TODO: Handle multiple threads per line (With a list)
            val pcs:Array[(Int,Int)] = interpreter.getPCs(line)
            if(pcs.length == 0)
                printf(line + ": " + lineString + " 0")
            else {
                printf(line + ": ")
                var oldPC = 0
                for((pc,num) <- pcs) {
                    if(pc < lineString.length) {
                        printf(lineString.substring(oldPC,pc))
                        if(useNums) {
                            printf(Console.RED + num)
                        }
                        else
                            printf(Console.RED + lineString.substring(pc,pc+1))
                        printf(Console.RESET)
                    }
                    oldPC = pc
                }
                printf(lineString.substring(oldPC))
                printf(" " + pcs.length)
            }
            println()
            line += 1
        }
        println()

        //Display data
        for(x:Int <- -10 to 10) {
            printf("|%1$4d".format(x))
        }
        println()
        for(x:Int <- -10 to 10) {
            printf("|%1$4d".format(interpreter.getData(x)))
        }
        println()
    }

    def helpCommands() {
        printf("%-30s%s\n", "s", "step all threads one instruction")
        printf("%-30s%s\n", "c", "continue execution until breakpoint or completion")
        printf("%-30s%s\n", "b {pc} {line}", "set breakpoint at specified program counter within the specified line")
        printf("%-30s%s\n", "s {line}", "steps all threads of the specified line type by one instruction")
        printf("%-30s%s\n", "s {line} {thread}", "steps the specified thread in the specified line")
        printf("%-30s%s\n", "t","displays the thread numbers for each position in code")

        println()
    }

    def getNumProcessesOfLine(interpreter: Interpreter, line: Int) = {
      val threads = interpreter.getThreads()
      if (threads == null) println("Null threads!")
      if (threads(line) == null) println("Null threads(line)!")
      threads(line).length
    }

    def getPC(interpreter: Interpreter, line: Int, instanceOfLine: Int): Int = {
      val threads = interpreter.getThreads()
      if (threads == null) println("Null threads!")
      if (threads(line) == null) println("Null threads(line)!")
      if (threads(line)(instanceOfLine) == null) println("Null threads(line)(process)!")
      if (threads(line).length > 0) {
        threads(line)(instanceOfLine).pc
      }
      else
        -1
    }

}
