import scala.io.Source
object Brainkkake {
    def main(args: Array[String]) {
        val parser = new Parser
        for (arg: String <- args) {
            val src = Source.fromFile(arg).mkString
            new Interpreter(parser.parse(src).get, false).runProgram()
        }
    }
}
