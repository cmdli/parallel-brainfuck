DIR=scala/src
SRC=$(DIR)/Parser.scala $(DIR)/TestExecutor.scala $(DIR)/TestParser.scala
all:
	scalac $(SRC)
