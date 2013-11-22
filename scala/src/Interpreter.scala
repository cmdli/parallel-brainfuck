import java.util.concurrent.atomic.AtomicInteger
/**
 * Performs the actions specified by the BK program.
 **/
class Interpreter {

    // Should be big enough
    val sizeOfData =  1000000

    // Makes a zeroed out array.
    var dataArr = Array.fill[AtomicInteger](sizeOfData)(new AtomicInteger(0))

    // Program starts in the middle of the "infinite" array
    var dataPointer = sizeOfData/2

    def runProgram(program: List[Operation]):Array[AtomicInteger] = {
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

    def add() = dataArr(dataPointer).incrementAndGet

    def subtract() = dataArr(dataPointer).decrementAndGet

    def printData() = print(dataArr(dataPointer).get.toChar)

    def scan() = dataArr(dataPointer).set(Console.in.read)

    def shiftRight() = dataPointer += 1

    def shiftLeft() = dataPointer -= 1

    //Run a loop by running the code inside of it while data is zero
    def loop(loopOperations: List[Operation]) = {
        while (dataArr(dataPointer).get != 0) {
            for (op: Operation <- loopOperations) {
                runOp(op)
            }
        }
    }

}

