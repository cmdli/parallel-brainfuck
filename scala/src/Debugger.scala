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
        println()
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
            case _ => println("Invalid instruction!")
        }
    }

    def displayLines(program:Array[String], interpreter:Interpreter, useNums:Boolean = false) {

        var line = 0
        while(line < program.length) {
            val lineString:String = program(line)
            val pcs:Array[(Int,Int)] = interpreter.getPCs(line)
            if(pcs.length == 0)
                printf(line + ": " + lineString + " 0 instances")
            else {
                printf(line + ": ")
                var oldPC = -1
                for((pc,num) <- pcs) {
                    if(pc < lineString.length) {
                        if(oldPC < pc)
                            printf(lineString.substring(oldPC+1,pc))
                        if(useNums)
                            printf(Console.RED + num)
                        else if(oldPC != pc){
                            if(interpreter.atPipe(line, num)) {
                                printf(Console.BLUE + lineString.substring(pc,pc+1))
                            }
                            else {
                                printf(Console.RED + lineString.substring(pc,pc+1))
                            }
                        }
                        printf(Console.RESET)
                        oldPC = pc
                    }
                }
                if(oldPC < lineString.length - 1)
                    printf(lineString.substring(oldPC+1))
                printf(" " + pcs.length + " instance(s)")
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
