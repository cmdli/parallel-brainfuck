/**
 * Provides a debugging interface to the interpreter
 */
class Debugger {

    var current_thread:Int = -1

    def control(instruction:String, interpreter:Interpreter) {
        /*instruction match {
            case "s" => interpreter.step(current_thread)
            case "c" => interpreter.continue()
            case "b"
        }*/
    }

    def display(program:List[String], interpreter:Interpreter) {
        var line = 0
        while(line < program.length) {
            val lineString = program(line)
            //TODO: Handle multiple threads per line (With a list)
            var pc:Int = 0//interpreter.getPC(line)
            print(line + ": " + lineString.substring(0,pc)
                    + Console.RED + lineString.substring(pc,pc+1)
                    + Console.RESET + lineString.substring(pc+1))
            line += 1
        }
    }

}
