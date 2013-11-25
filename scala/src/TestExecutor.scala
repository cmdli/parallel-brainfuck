object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()

        // Prints "2", verify loop within loop
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        var executor = new Interpreter(new Program(programOutput))
        executor.runProgram()

        // Prints "Hello World!\n"
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor = new Interpreter(new Program(programHello))
        executor.runProgram()

        // Verify input/output
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        executor = new Interpreter(new Program(programInput))
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

        // Verify copy by value
        val programValue:List[List[Operation]] = parser.parse("+>,>,<<*>....\n>>....").get
        executor = new Interpreter(new Program(programValue))
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

        // Verify pipe
        val programPipe:List[List[Operation]] = parser.parse("+>,>,<<*>.|.|.|.\n1234567>>.|.|.|.\n").get
        executor = new Interpreter(new Program(programPipe))
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

	// Print "ABCDEF" without threads
 	val programPrintSequence:List[List[Operation]] = parser.parse("+++++[>+++++++++++++<-]>-<++++++[>+.<-]").get
        executor = new Interpreter(new Program(programPrintSequence))
        print("\nEnter 2 chars: ")
        executor.runProgram()
        println()

	// Prints "ABCDEF" using threads and syncing between threads within a loop
	val programLoopSync:List[List[Operation]] = parser.parse("+*++++[>+++++++++++++<-]>-<++++++|[>+|m<-]\n012345678901234567890123456789012|[>m|,<-]").get
	executor = new Interpreter(new Program(programLoopSync))
	//executor.runProgram()
	println()

    }
}
