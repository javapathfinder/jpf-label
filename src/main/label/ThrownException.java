/*
 * Copyright (C) 2020  Syyeda Zainab Fatmi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */
package label;

import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.bytecode.ATHROW;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * A labeling function for a thrown (and handled) exception or error (that is,
 * subclasses of java.lang.Throwable).
 * 
 * The exceptions/errors to be labeled can be specified in the application
 * properties file by setting the property label.ThrownException.
 * Exception/error signatures must be in the format: package.class
 * 
 * @author Syyeda Zainab Fatmi
 */
public class ThrownException extends TransitionLabelMaker {
	private String[] exceptionName; // exception signature
	private String exceptionThrown; // the next exception to be thrown

	/**
	 * Initializes this labeling function.
	 *
	 * @param configuration JPF's configuration
	 */
	private ThrownException(Config configuration) {
		exceptionName = getConfiguredProperty(configuration, "label.ThrownException.type");
		exceptionThrown = null;
	}

	/**
	 * Creates a ThrownException object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static ThrownException getInstance(Config configuration) {
		return new ThrownException(configuration);
	}

	@Override
	public Set<Label> breakAfter(Instruction executedInstruction) {
		if (executedInstruction instanceof ATHROW) {
			for (int i = 0; i < exceptionName.length; i++) {
				if (exceptionThrown.equals(exceptionName[i])) {
					Set<Label> labels = new HashSet<Label>();
					labels.add(new Label(exceptionThrown.replaceAll("[$.]", "_"), exceptionThrown));
					return labels;
				}
			}
		}
		return null;
	}

	@Override
	public void beforeInstruction(Instruction instructionToExecute) {
		exceptionThrown = null;
		if (instructionToExecute instanceof ATHROW) {
			ThreadInfo ti = ThreadInfo.getCurrentThread();
			StackFrame frame = ti.getModifiableTopFrame();
			int objref = frame.peek();
			if (objref == 0) { // MJI Null
				exceptionThrown = "java.lang.NullPointerException";
			} else {
				VM vm = VM.getVM();
				Heap heap = vm.getHeap();
				ElementInfo eiException = heap.get(objref);
				if (eiException != null) {
					ClassInfo ciException = eiException.getClassInfo();
					exceptionThrown = ciException.getName();
				}
			}
		}
	}
}
