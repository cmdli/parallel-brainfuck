object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()
        val isDebugging = false

        // Prints "2", verify loop within loop
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        var executor = new Interpreter(programOutput, isDebugging)
        println("Expected: 2")
        executor.runProgram()
        println
        if (isDebugging) {
          // To play with debugging between two threads
          val programDebugging:List[List[Operation]] = parser.parse("+*>><<>+|+|+|+\n1234567>>+|+|+|+\n").get
          executor = new Interpreter(programDebugging, isDebugging)
          println("\nUse this to play with debugging multiple threads. ** Make sure that the second process has been forked **")
          executor.runProgram()
          println()
        }

        // Prints "Hello World!\n"
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor = new Interpreter(programHello, isDebugging)
        println("\nExpected: Hello World!\\n")
        executor.runProgram()
        println

        val programHelloPara = parser.parse("+*| ++++ +++++ |[ >+++++ ++    <-|]\n" +
                                            "* |            |[ >>+++++ +++++<<|]\n" +
                                            "* |            |[ >>>+++      <<<|]\n" +
                                            "  |            |[ >>>> +     <<<<|]+*\n" +
                                            "> ++ .<*> > +. +++++ ++. . +++. > ++ .| | > . +++ . ----- -. ----- ---. > + . > .\n").get
        executor = new Interpreter(programHelloPara, isDebugging)
        println("\nExpected: Hello World!\\n")
        executor.runProgram()
        println

        // Verify input/output
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        executor = new Interpreter(programInput, isDebugging)
        println("\nExpected: The first two characters you entered")
        print("Enter 6 chars: ")
        executor.runProgram()
        println()

        // Verify copy by value
        val programValue:List[List[Operation]] = parser.parse("+>,>,<<*>....\n>>....").get
        executor = new Interpreter(programValue, isDebugging)
        println("\nExpected: eight characters")
        executor.runProgram()
        println()

//        // Verify pipe
//        val programPipe:List[List[Operation]] = parser.parse("+>,>,<<*>.|.|.|.\n1234567>>.|.|.|.\n").get
//        executor = new Interpreter(programPipe, isDebugging)
//        println("\nExpected: four adjacent pairs of the same characters")
//        executor.runProgram()
//        println()

        // Print "ABCDEF" without threads
        val programPrintSequence:List[List[Operation]] = parser.parse("+++++[>+++++++++++++<-]>-<++++++[>+.<-]").get
        executor = new Interpreter(programPrintSequence, isDebugging)
        println("\nExpected: ABCDEF")
        executor.runProgram()
        println()

        // Prints "ABCDEF" using threads and syncing between threads within a loop
        val programLoopSync:List[List[Operation]] = parser.parse("+*++++[>+++++++++++++<-]>-<++++++|[>+|m<-]\n012345678901234567890123456789012|[>m|,<-]").get
        executor = new Interpreter(programLoopSync, isDebugging)
        //executor.runProgram()
        println()

        // Prints input character 36 times and verifies that we can clone concurrently without error
        executor = new Interpreter(
              parser.parse(
                  "+++++++[>+++++++<-]>-->+*\n******\n******\n<+."
              ).get,
              isDebugging
        )
        println("Expected: 36 characters ending with S")
        executor.runProgram()
        println
    }
}
