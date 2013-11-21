/**
 * Performs the actions specified by the BK program.
 */
class BrainkkakeExecutor {
  // TODO: Make this dynamic?
  val sizeOfData = 100

  // Makes a zeroed out array.
  var dataArr = Array.fill[Int](sizeOfData)(0)

  // TODO: We can't go backwards (left) of the start like this.
  var dataPointer = 0

  def runProgram(program: List[Operation]):Array[Int] = {
        for (op: Operation <- program) {
            runOp(op)
        }
        dataArr
    }

  def runOp(op: Operation): Unit = op match {
    case AddOperation() => add()
    case SubOperation() => subtract()
    case PrintOperation() => printData()
    case ShiftRightOperation() => shiftRight()
    case ShiftLeftOperation() => shiftLeft()
    case LoopOperations(operations) => maybePerformLoop(operations)
  }

  def add() = dataArr(dataPointer) += 1

  def subtract() = dataArr(dataPointer) -= 1

  def printData() = print(dataArr(dataPointer).toChar)

  def shiftRight() = dataPointer += 1

  def shiftLeft() = dataPointer -= 1

  def maybePerformLoop(loopOperations: List[Operation]) = {
    while (dataArr(dataPointer) != 0) {
      for (op: Operation <- loopOperations) {
        runOp(op)
      }
    }
  }

}
