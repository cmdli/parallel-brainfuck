import scala.collection.mutable

/**
 * Provides a debugging interface to the interpreter
 */
class Debugger {

    /*val StepLinePattern = "([0-9]+)".r
    val BreakpointPattern1 = "(b\\s+)([0-9]+)".r
    val BreakpointPattern2 = "(b\\s+)([0-9]+)(\\s+)([0-9]+)".r
    val SwitchPattern = "(switch\\s+)([0-9]+)".r
    val DataPattern = "(data\\s+)([0-9]+)".r



    def control(instruction:String, interpreter:Interpreter) {
        instruction match {
            case StepLinePattern(line) => interpreter.step(line) //Step the specified line
            case "c" => interpreter.continue() //Continue until a thread hits a breakpoint
            //case BreakpointPattern1(_, pc) => interpreter.addBreakpoint(pc)
            case BreakpointPattern2(_, pc, _, line) => interpreter.addBreakpoint(pc,line)
        }
    }

    println("\nCurrent instruction: " + lineOps(pc) + " at pc: " + pc + " in line: " + line)
    println("What do you want to do? (h for help)")

    def display(program:List[String], interpreter:Interpreter) {
        //Display code
        var line = 0
        while(line < program.length) {
            val lineString = program(line)
            //TODO: Handle multiple threads per line (With a list)
            var pc:Int = interpreter.getPC(line)
            print(line + ": " + lineString.substring(0,pc)
                    + Console.RED + lineString.substring(pc,pc+1)
                    + Console.RESET + lineString.substring(pc+1))
            println()
            line += 1
        }
        println()

        //Display data
        for(x <- Range(-10,10)) {
            printf("|%1$4".format(x))
        }
        println()
        for(x <- Range(-10,10)) {
            printf("|%1$4".format(interpreter.getData(x)))
        }
    }*/


}
