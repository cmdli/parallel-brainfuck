BINDIR=scala/classes
SRCDIR=scala/src
SRC=$(SRCDIR)/*.scala
all:
	mkdir -p $(BINDIR)
	scalac $(SRC) -d $(BINDIR)
exec:
	scala -classpath $(BINDIR) TestExecutor
parse:
	scala -classpath $(BINDIR) TestParser
debug:
	scala -classpath $(BINDIR) TestDebugger
run:
	scala -classpath $(BINDIR) Brainkkake examples/hello-alter.bk
%:
	scala -classpath $(BINDIR) Brainkkake examples/$*
