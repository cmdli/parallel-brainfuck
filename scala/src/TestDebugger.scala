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

    }

}
