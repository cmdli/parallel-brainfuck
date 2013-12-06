# Parallel Brainfuck

A parallel version of Brainfuck implemented using a Scala DSL.

How it works: <br />
Each line of parallel brainfuck is a code sequence for one thread. <br />
The first line always starts off with one thread. <br />
v and ^ clone a thread from the line below or above the current thread. <br />
\* clones the current thread. <br />
A | in a column waits for all other active instances with | in the same column to reach that instruction. <br />

Example parallel hello world:

```
+v ++++ +++++ [| >+++++ ++    <-|]
v             [| >>+++++ +++++<<|]
v             [| >>>+++      <<<|]
              [| >>>> +     <<<<|]v
> ++ . v > +. +++++ ++. . +++. > ++ .| | < . +++ . ----- -. ----- ---. > + . > .
       +++++ +++++ +++++             |.|
```

Presentation: https://docs.google.com/presentation/d/1Y8uwsh_5bmUASmyRE4i3S4fL5OvA2Q92edivIXpsEaM/edit?usp=sharing

To compile: 
make all

Running modes:
./bk {filepath} -- run any parallel brainfuck code
./bk debug {filepath} -- run code in debugging mode
make exec -- run small test suite
 

Note: The idea to use RegexParser and case classes in parsing came from a quick glance at the code from this project: https://github.com/j5ik2o/brainfuck-scala
