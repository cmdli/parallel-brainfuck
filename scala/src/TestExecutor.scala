object TestExecutor {
   def main(args: Array[String]) {
      val parser = new Parser()
      val executor = new Interpreter()

      // Prints "#"
      val programOutput:List[Operation] = parser.parse("+++++[>+++++++<-]>.").get
      executor.runProgram(programOutput)

      // Prints input char
      val programInput:List[Operation] = parser.parse(",.").get
      println("\nEnter a char: ")
      executor.runProgram(programInput)

   }
}
