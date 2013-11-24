/**
 * Tests BK Parser
 */

object TestParser extends Parser {
    def main(args: Array[String]) {
        val ops:List[List[Operation]] = parse("[+[-]|+|][]\n0[23]|6||*f").get
        val program: Program = new Program(ops)

        println(program.calculateOperations())
        println(program.calculateBlockMap())
    }
}
