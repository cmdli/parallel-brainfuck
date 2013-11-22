BINDIR=scala/classes
SRCDIR=scala/src
SRC=$(SRCDIR)/*.scala
all:
	mkdir -p $(BINDIR)
	scalac $(SRC) -d $(BINDIR)
