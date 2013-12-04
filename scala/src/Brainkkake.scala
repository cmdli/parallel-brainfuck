import scala.io.Source
object Brainkkake {
    def main(args: Array[String]) {
        val parser = new Parser
        if(args(0) == "debug") {
            var debug = new Debugger
            for (j <- 1 to (args.length-1)) {
                val arg:String = args.apply(j)
                val src = Source.fromFile(arg).mkString
                var i = new Interpreter(parser.parse(src).get)
                debug.debug(src.split("\n"), i)
            }
        }

        for (arg: String <- args) {
            val src = Source.fromFile(arg).mkString
            new Interpreter(parser.parse(src).get).runProgram()
        }
    }
}
