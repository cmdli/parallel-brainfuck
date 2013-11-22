
object TestExecutor {
    def main(args: Array[String]) {
        val parser = new Parser()

        // Prints "#"
        var executor = new Interpreter()
        val programOutput: List[List[Operation]] = parser.parse("+++++[>+++++[>++<-]<-]>>.").get
        executor.runProgram(programOutput)
        // Prints "Hello World!\n"
        executor = new Interpreter()
        val programHello:List[List[Operation]] = parser.parse("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.").get
        executor.runProgram(programHello)

        // Prints input char
        executor = new Interpreter()
        val programInput:List[List[Operation]] = parser.parse(",>,<.>.").get
        print("\nEnter 2 chars: ")
        executor.runProgram(programInput)
    }
}
