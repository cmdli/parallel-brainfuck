import scala.collection.mutable

/**
 * Provides a debugging interface to the interpreter
 */
class Debugger {

    val StepLinePattern = "([0-9]+)".r
    val BreakpointPattern1 = "(b\\s+)([0-9]+)".r
    val BreakpointPattern2 = "b\\s+([0-9]+)\\s+([0-9]+)".r
    val SwitchPattern = "(switch\\s+)([0-9]+)".r
    val DataPattern = "(data\\s+)([0-9]+)".r

    def debug(program:Array[String], interpreter:Interpreter) {
        var running = true
        interpreter.startProgram()
        while(interpreter.getNumThreads() > 0) {
            display(program, interpreter)
            print("(h for help) >")
            val command = readLine()
            println()
            control(command,interpreter)
        }
        interpreter.stopProgram()
    }


    def control(instruction:String, interpreter:Interpreter) {
        instruction match {
            case StepLinePattern(line) => println("Stepping line " + line + "...");interpreter.step(line.toInt) //Step the specified line
            case "s" => println("Stepping all threads...");interpreter.stepAll()
            case "c" => println("Continuing...");interpreter.continue() //Continue until a thread hits a breakpoint
            //case BreakpointPattern1(_, pc) => interpreter.addBreakpoint(pc)
            case BreakpointPattern2(pc,line) => println("Breakpoint at (%d,%d)", pc, line); interpreter.addBreakpoint(pc.toInt,line.toInt)
            case "h" => helpCommands()
        }
    }

    def display(program:Array[String], interpreter:Interpreter) {
        //Display code
        var line = 0
        while(line < program.length) {
            val lineString = program(line)
            //TODO: Handle multiple threads per line (With a list)
            var pc:Int = interpreter.getPC(line)
            if(pc < 0)
                print(line + ": " + lineString)
            else {
                print(line + ": " + lineString.substring(0,pc)
                        + Console.RED + lineString.substring(pc,pc+1)
                        + Console.RESET + lineString.substring(pc+1))
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
        printf("%-30s%s\n", "{line}", "steps all threads of the specified line type by one instruction")

        println()
    }


}
