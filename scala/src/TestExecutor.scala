object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()


        // Prints "2", verify loop within loop
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        var executor = new Interpreter(new Program(programOutput))
        println("Expected: 2")
        executor.runProgram()
        println

        // Prints "Hello World!\n"
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor = new Interpreter(new Program(programHello))
        println("\nExpected: Hello World!\\n")
        executor.runProgram()
        println

        val programHelloPara = parser.parse("+* ++++ +++++ |[ >+++++ ++    <-|]\n" +
                                            "*             |[ >>+++++ +++++<<|]\n" +
                                            "*             |[ >>>+++      <<<|]\n" +
                                            "              |[ >>>> +     <<<<|]+*\n" +
                                            "> ++ .<*> > +. +++++ ++. . +++. > ++ .| | > . +++ . ----- -. ----- ---. > + . > .\n" +
                                            "+++++ +++++ +++++                     |.|\n").get
        executor = new Interpreter(new Program(programHelloPara))
        println("\nExpected: Hello World!\\n")
        executor.runProgram()
        println

        // Verify input/output
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        executor = new Interpreter(new Program(programInput))
        println("\nExpected: The first two characters you entered")
        print("Enter 6 chars: ")
        executor.runProgram()
        println()

        // Verify copy by value
        val programValue:List[List[Operation]] = parser.parse("+>,>,<<*>....\n>>....").get
        executor = new Interpreter(new Program(programValue))
        println("\nExpected: eight characters")
        executor.runProgram()
        println()

        // Verify pipe
        val programPipe:List[List[Operation]] = parser.parse("+>,>,<<*>.|.|.|.\n1234567>>.|.|.|.\n").get
        executor = new Interpreter(new Program(programPipe))
        println("\nExpected: four adjacent pairs of the same characters")
        executor.runProgram()
        println()

        // Print "ABCDEF" without threads
        val programPrintSequence:List[List[Operation]] = parser.parse("+++++[>+++++++++++++<-]>-<++++++[>+.<-]").get
        executor = new Interpreter(new Program(programPrintSequence))
        println("\nExpected: ABCDEF")
        executor.runProgram()
        println()

        // Prints "ABCDEF" using threads and syncing between threads within a loop
        val programLoopSync:List[List[Operation]] = parser.parse("+*++++[>+++++++++++++<-]>-<++++++|[>+|m<-]\n012345678901234567890123456789012|[>m|,<-]").get
        executor = new Interpreter(new Program(programLoopSync))
        //executor.runProgram()
        println()

        // Prints input character 36 times and verifies that we can clone concurrently without error
        executor = new Interpreter(
            new Program(
                parser.parse(
                    "+++++++[>+++++++<-]>-->+*\n******\n******\n<+."
                ).get
            )
        )
        println("Expected: 36 characters ending with S")
        executor.runProgram()
        println
    }
}
