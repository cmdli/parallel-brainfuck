/**
 * Provides a debugging interface to the interpreter
 */
class Debugger {

    def display(program:List[String], interpreter:Interpreter) {
        var line = 0
        while(line < program.length) {
            val lineString = program(line)
            //TODO: Handle multiple threads per line (With a list)
            var pc:Int = interpreter.getPC(line)
            print(line + ": " + lineString.substring(0,pc)
                    + Console.RED + lineString.substring(pc,pc+1)
                    + Console.RESET + lineString.substring(pc+1));
            line += 1
        }
    }

}
