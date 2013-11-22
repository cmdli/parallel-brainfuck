BINDIR=scala/classes
SRCDIR=scala/src
SRC=$(SRCDIR)/*.scala
all:
	mkdir -p $(BINDIR)
	scalac $(SRC) -d $(BINDIR)
run:
	scala -classpath $(BINDIR) TestExecutor
parse:
	scala -classpath $(BINDIR) TestParser
