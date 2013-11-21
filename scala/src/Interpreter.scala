/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter {

    // Should be big enough
    val sizeOfData =  1000000

    // Makes a zeroed out array.
    var dataArr = Array.fill[Byte](sizeOfData)(0)

    // Program starts in the middle of the "infinite" array
    var dataPointer = sizeOfData/2

    def runProgram(program: List[Operation]):Array[Byte] = {
        for (op: Operation <- program) {
            runOp(op)
        }
        dataArr
    }

    //Run one operation
    def runOp(op: Operation): Unit = op match {
        case AddOperation() => add()
        case SubOperation() => subtract()
        case PrintOperation() => printData()
        case InputOperation() => scan()
        case ShiftRightOperation() => shiftRight()
        case ShiftLeftOperation() => shiftLeft()
        case LoopOperations(operations) => loop(operations)
    }

    def add() = dataArr(dataPointer) += 1

    def subtract() = dataArr(dataPointer) -= 1

    def printData() = print(dataArr(dataPointer).toChar)

    def scan() = dataArr(dataPointer) = readChar()

    def shiftRight() = dataPointer += 1

    def shiftLeft() = dataPointer -= 1

    //Run a loop by running the code inside of it while data is zero
    def loop(loopOperations: List[Operation]) = {
        while (dataArr(dataPointer) != 0) {
            for (op: Operation <- loopOperations) {
                runOp(op)
            }
        }
    }

}

