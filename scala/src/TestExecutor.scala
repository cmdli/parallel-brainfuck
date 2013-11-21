object TestExecutor {
   def main(args: Array[String]) {
      val parser = new Parser()
      val executor = new BrainkkakeExecutor()

      // Prints "#"
      val program:List[Operation] = parser.parse("+++++[>+++++++<-]>.").get
      executor.runProgram(program)
   }
}
