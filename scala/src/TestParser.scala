/**
 * Tests BK Parser
 */

object TestParser extends Parser {
     def main(args: Array[String]) {
         val addOperation:List[Operation] = parse("++").get

         if (addOperation(1).isInstanceOf[AddOperation]) {
           println("Success!")
         }
     }
}
