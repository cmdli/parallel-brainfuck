
object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()

        // Prints "2"
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        var executor = new Interpreter(programOutput)
        executor.runProgram()
        // Prints "Hello World!\n"
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor = new Interpreter(programHello)
        executor.runProgram()

        // Verify input
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        executor = new Interpreter(programInput)
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()
        // Verify copy by value
        val programPara:List[List[Operation]] = parser.parse("+>,>,<<f>....\n>>....\n").get
        executor = new Interpreter(programPara)
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()
    }
}
