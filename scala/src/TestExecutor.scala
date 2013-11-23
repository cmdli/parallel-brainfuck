
object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()

        // Prints "2"
        var executor = new Interpreter()
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        executor.runProgram(programOutput)
        // Prints "Hello World!\n"
        executor = new Interpreter()
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor.runProgram(programHello)

        // Verify input
        executor = new Interpreter()
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        print("\nEnter 2 chars: ")
        executor.runProgram(programInput)
        println()
        // Verify copy by value
        executor = new Interpreter()
        val programPara:List[List[Operation]] = parser.parse(",>,<f>...\n....\n").get
        print("\nEnter 2 chars: ")
        executor.runProgram(programPara)
        println()
    }
}
