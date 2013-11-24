import scala.collection.immutable.HashMap
import scala.collection.mutable

class Program(parsedProgram: List[List[Operation]]) {

  private var operations: Option[List[List[Operation]]] = None

  // for pipe operator
  // column -> line numbers
  private var blockMap: Option[Map[Int, mutable.LinkedList[Int]]] = None

  // The match structure ensures that we only call finalizeProgram() once.
  def calculateOperations(): List[List[Operation]] = operations match {
    case None => finalizeProgram(); operations.get
    case Some(ops) => ops
  }

  // The match structure ensures that we only call finalizeProgram() once.
  def calculateBlockMap(): Map[Int, mutable.LinkedList[Int]] = blockMap match {
    case None => finalizeProgram(); blockMap.get
    case Some(map) => map
  }


  // Should only be called once.
  // Updates loop operations to point to the start and finished pcs.
  // Creates the block map from PC to the line numbers which have pipes at that pc.
  private def finalizeProgram(): Unit = {
    if (blockMap == None) blockMap = Some(new HashMap[Int, mutable.LinkedList[Int]])
    if (operations == None) operations = Some(parsedProgram)

    var lineNumber: Int = 0
    for (line <- operations.get) {
      finalizeLine(line, lineNumber)
      lineNumber += 1
    }
  }

  private def finalizeLine(line: List[Operation], lineNumber: Int): Unit = {
    val starts: mutable.Stack[StartLoopOperation] = new mutable.Stack()
    var pc = 0

    for (op <- line) {
      op match {
        // Stores the StartLoopOp in a stack to be later matched with an EndLoopOp.
        case op:StartLoopOperation => {
          // Temporarily set endPC to the startOp's PC so that we can retrieve
          // it's value when we match it to the EndLoopOperation.
          op.endPC = pc
          starts.push(op)
        }
        // Sets the end to point to the start of the loop and vice versa.
        case op:EndLoopOperation => {
          val start:StartLoopOperation = starts.pop()
          val startPC = start.endPC
          op.startPC = startPC
          start.endPC = pc
        }
        // Updates the block map from PC to the line numbers which have pipes at that pc.
        case op:PipeOperation => {
          val set: mutable.LinkedList[Int] = blockMap.get get pc match {
            case Some(b) => b
            case None => new mutable.LinkedList[Int]
          }
          blockMap = Some(blockMap.get.updated(pc, lineNumber +: set))
        }
        case _ => ()
      }

      pc += 1
    }
  }

}
