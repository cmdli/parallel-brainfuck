/**
 * Tests BK Parser
 */

object TestParser extends Parser {
    def main(args: Array[String]) {
      val ops:List[List[Operation]] = parse("[+[-]|+|][]\n0[23]|6||*f").get

      println(ops)
    }
}
