object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()

        // Prints "2", verify loop within loop
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        var executor = new Interpreter(programOutput)
        executor.runProgram()

        // Prints "Hello World!\n"
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor = new Interpreter(programHello)
        executor.runProgram()

        // Verify input/output
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        executor = new Interpreter(programInput)
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

        // Verify copy by value
        val programValue:List[List[Operation]] = parser.parse("+>,>,<<f>....\n>>....").get
        executor = new Interpreter(programValue)
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

        // Verify pi|pe
        val programPipe:List[List[Operation]] = parser.parse("+>,>,<<f>.|.|.|.\n1234567>>.|.|.|.\n").get
        executor = new Interpreter(programPipe)
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

    }
}
