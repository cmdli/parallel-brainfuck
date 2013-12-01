/**
 * Tests Debugger.scala
 */
object TestDebugger {
    def main(args:Array[String]) {
        val parser = new Parser()
        val debug = new Debugger()

        val source = "+*++++[>+++++[>++<-]<-]>>."
        val program = parser.parse(source).get
        val i = new Interpreter(program, true)
        debug.debug(source.split("\n"),i)
    }
}
