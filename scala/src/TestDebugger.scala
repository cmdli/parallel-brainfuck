/**
 * Tests Debugger.scala
 */
object TestDebugger {
    def main(args:Array[String]) {
        val parser = new Parser()
        val debug = new Debugger()

        val source = "+**++++[>+++++[>++<-]<-]>>.\n<+|"
        val program = parser.parse(source).get
        println(program)
        val i = new Interpreter(program)
        debug.debug(source.split("\n"),i)

        val sourceHelloPara = "+*|++++ +++++|[| >+++++ ++    <-|]\n" +
                              "* |          |[| >>+++++ +++++<<|]\n" +
                              "* |          |[| >>>+++      <<<|]\n" +
                              "  |          |[| >>>> +     <<<<|] +*\n" +
                              "> ++ .<*> > +. +++++ ++. . +++. > ++ .| | < . +++ . ----- -. ----- ---. > + . > .\n" +
                              ">+++++ +++++ +++++                    |.|\n"
        val programHelloPara = parser.parse(sourceHelloPara).get
        val j = new Interpreter(programHelloPara)
        debug.debug(sourceHelloPara.split("\n"),j)

        val sourceHelloParaFork = "v +++++ +++++|[| >+++++ ++    <-|]\n" +
                                  "v            |[| >>+++++ +++++<<|]\n" +
                                  "v            |[| >>>+++      <<<|]\n" +
                                  "             |[| >>>> +     <<<<|]v\n" +
                                  "> ++ .v > +. +++++ ++. . +++. > ++ .| | < . +++ . ----- -. ----- ---. > + . > .\n" +
                                  "+++++ +++++ +++++                   |.|\n"
        val programHelloParaFork = parser.parse(sourceHelloParaFork).get
        val k = new Interpreter(programHelloParaFork)
        debug.debug(sourceHelloParaFork.split("\n"),k)

        val sourceExpo = "vvvv|.\n" +
                         "vvvv|\n" +
                         "++++|\n"
        val programExpo = parser.parse(sourceExpo).get
        val expoI = new Interpreter(programExpo)
        debug.debug(sourceExpo.split("\n"), expoI)

    }

}
