Overview
--------

jpf-label is a basic extension of Java PathFinder (JPF) which provides an
easy way to label states of the state space generated by JPF.  To capture
simple known facts about the states of JPF's virtual machine, jpf-label
labels those states with a set of atomic propositions. We can use
jpf-label to either produce a file that describes the labelling of the
states or enhance the graphical representation of the state space,
as already provided by JPF, with colouring the states according to their
labelling.

In order to use jpf-label, a labelling function is required
to label states with desired atomic properties must be
implemented and specified in the application properties
file by setting the property label.class. The following
classes to label states are provided as part of the package
label.  For some classes an additional property needs to be specified as
indicated below.

1. `Initial`: labels of the initial state.

2. `End`: labels the final states (also known as end states).

3. `AllDifferent`: labels each state with a different label.

4. `BooleanStaticField`: labels those states in which the static boolean
   field specified by the property label.StaticBooleanField.field is true.

5. `PositiveIntegerLocalVariable`: labels those states in which the local
   integer variable specified by the property
   label.LocalPositiveIntegerVariable.variable is positive.

6. `InvokedStaticMethod`: labels those states in which the method
   specified by the property label.InvokedStaticMethod.method is invoked.

7. `ReturnedVoidMethod`: labels those states in which the void method
   specified by the property label.ReturnedVoidMethod.method has returned.

8. `ReturnedBooleanMethod`: labels those states in which the boolean
   method specified by the property label.ReturnedBooleanMethod.method
   has returned, with the return value.

9. `ThrownException`: labels those states in which an exception of the
   type specified by the property label.ThrownException.type has
   been thrown.

10. `SynchronizedStaticMethod`: labels those states in which the
    synchronized method specified by the property
    label.SynchronizedStaticMethod.method acquires and has released the
    lock.

Our extension jpf-label provides a framework that allows users to easily
define their own state labelling, by implementing either of the interfaces
`StateLabelMaker` or `TransitionLabelMaker`.

To exhibit the functionality of jpf-label, let us consider the following
simple example in which the static method setValue is invoked when
random.nextBoolean() returns true.

```java
import java.util.Random;

public class Method {
  private static int value;

  public static void setValue(int value) {
    Method.value = value;
  }

  public static void main(String[] args) {
    Random random = new Random();
    if (random.nextBoolean()) {
      Method.setValue(2);
    }
  }
}
```

JPF, extended with jpf-label and configured appropriately, can write the
labelled state space underlying the above code, represented in DOT
format, to a file, so that it can be viewed using dotty.  For example,
if we use the following configuration file to label the initial state,
final states, and those states in which the static method setValue is
invoked

    target = Method
    classpath = <path to the directory containing Method.class>
    cg.enumerate_random = true

    @using jpf-label
    listener = label.StateLabelDot
    label.class = label.Initial; label.End; label.InvokedStaticMethod
    label.InvokedStaticMethod.method = Method.setValue(int)

then JPF produces a file named Method.dot with the following content.

    digraph statespace {
    node [colorscheme="set312" style=wedged]
    -1 [style=filled fillcolor=1]
    -1 -> 0
    1 [style=filled fillcolor=2]
    0 -> 1
    2 [style=filled fillcolor=3]
    0 -> 2
    3 [style=filled fillcolor=2]
    2 -> 3
    }

JPF, extended with jpf-label and configured appropriately, can
also describe the labels in a text file.  For example, if we use the
following configuration file to label the initial state, final states,
and those states in which the static method setValue is invoked

    target = Method
    classpath = <path to the directory containing Method.class>
    cg.enumerate_random = true

    @using jpf-label
    listener = label.StateLabelText
    label.class = label.Initial; label.End; label.InvokedStaticMethod
    label.InvokedStaticMethod.method = Method.setValue(int)

the resulting file Method.lab contains the following text.

    0="initial" 1="end" 2="invoked__Method_setValue__I__V"
    -1: 0
    1: 1
    2: 2
    3: 1

The first line contains an enumeration of all the labels and their index,
which is a non-negative integer. The remaining lines  contain the
labelling of a state.  This is composed of the state followed by (the
indices of) the labels of that state.  Note that states are represented by
either -1 or a non-negative integer.  States that do not have any label
are not included in the file.

Furthermore, our extension also enables the user to construct a custom
format for the output of the labelling, by extending the abstract class
`StateLabel`.

Licensing of jpf-label
----------------------

This extension is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This extension is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You can find a copy of the GNU General Public License at
http://www.gnu.org/licenses

Installing of jpf-label
-----------------------

To install jpf-label, follow the steps below.

1. Install JPF.
   See https://github.com/javapathfinder/jpf-core/wiki/How-to-install-JPF
   for details.

2. Install jpf-label.

3. Build jpf-label using gradle.

4. Add jpf-label to the site.properties file.
