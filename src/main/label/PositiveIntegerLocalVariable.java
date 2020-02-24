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
import gov.nasa.jpf.jvm.bytecode.IINC;
import gov.nasa.jpf.jvm.bytecode.ISTORE;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.util.VarSpec;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * A labeling function for a positive integer local variable.
 * 
 * The variables to be labeled can be specified in the application properties
 * file by setting the property label.PositiveIntegerLocalVariable. Variable
 * signatures must be in the following format:
 * package.class.methodSignature:variableName
 * 
 * @author Syyeda Zainab Fatmi
 */
public class PositiveIntegerLocalVariable extends TransitionLabelMaker {
	private String[] varName; // variable signature
	private Boolean previousValue; // true if the previous value was positive, false otherwise

	private String lastModified; // the name of last modified variable
	private Integer lastValue; // the value of last modified variable
	private MethodInfo lastMethod; // the method which the last modified variable belongs to

	/**
	 * Initializes this labeling function.
	 */
	private PositiveIntegerLocalVariable(Config configuration) {
		varName = getConfiguredProperty(configuration, "label.PositiveIntegerLocalVariable.variable");
		lastModified = null;
		lastValue = null;
		lastMethod = null;
		previousValue = null;
	}

	/**
	 * Creates a PositiveIntegerLocalVariable object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static PositiveIntegerLocalVariable getInstance(Config configuration) {
		return new PositiveIntegerLocalVariable(configuration);
	}

	@Override
	public Set<String> getStateLabels(Search search) {
		Set<String> labels = new HashSet<String>();
		for (String var : varName) {
			Integer value = getValue(var);
			if (value != null && value.compareTo(0) > 0) {
				// label the state when the local variable is positive
				labels.add(getSignature(var));
			}
		}
		return labels;
	}

	@Override
	public Set<String> breakAfter(Instruction executedInstruction) {
		// break the transition after the local variable instruction, if the variable
		// value has changed from positive to negative or vice versa
		MethodInfo mi = executedInstruction.getMethodInfo();
		this.lastMethod = mi;
		int pc = executedInstruction.getPosition();

		if (executedInstruction instanceof ISTORE) {
			ISTORE instruction = (ISTORE) executedInstruction;
			int slotIdx = instruction.getLocalVariableIndex();
			for (String var : varName) {
				LocalVarInfo localVar = VarSpec.createVarSpec(var).getMatchingLocalVarInfo(mi, pc, slotIdx);
				if (localVar != null) {
					this.lastModified = var;
					this.lastValue = ThreadInfo.getCurrentThread().getTopFrame().getLocalVariable(slotIdx);
					if (!Boolean.valueOf(this.lastValue.compareTo(0) > 0).equals(this.previousValue)) {
						return new HashSet<String>();
					}
				}
				// if the scope would begin on the next instruction after the xSTORE
				localVar = VarSpec.createVarSpec(var).getMatchingLocalVarInfo(mi, pc + 1, slotIdx);
				if (localVar != null && localVar.getStartPC() == pc + 1) {
					this.lastModified = var;
					this.lastValue = ThreadInfo.getCurrentThread().getTopFrame().getLocalVariable(slotIdx);
					if (!Boolean.valueOf(this.lastValue.compareTo(0) > 0).equals(this.previousValue)) {
						return new HashSet<String>();
					}
				}
			}
		} else if (executedInstruction instanceof IINC) {
			IINC instruction = (IINC) executedInstruction;
			int slotIdx = instruction.getIndex(); // local variable slot
			for (String var : varName) {
				LocalVarInfo localVar = VarSpec.createVarSpec(var).getMatchingLocalVarInfo(mi, pc, slotIdx);
				if (localVar != null) {
					this.lastModified = var;
					this.lastValue = ThreadInfo.getCurrentThread().getTopFrame().getLocalVariable(slotIdx);
					if (!Boolean.valueOf(this.lastValue.compareTo(0) > 0).equals(this.previousValue)) {
						return new HashSet<String>();
					}
				}
			}
		}
		this.lastModified = null;
		return null;
	}

	@Override
	public void beforeInstruction(Instruction instructionToExecute) {
		// get the value before the local variable instruction
		Integer slotIdx = null;
		if (instructionToExecute instanceof ISTORE) {
			slotIdx = ((ISTORE) instructionToExecute).getLocalVariableIndex();
		} else if (instructionToExecute instanceof IINC) {
			slotIdx = ((IINC) instructionToExecute).getIndex();
		}
		if (slotIdx != null) {
			MethodInfo mi = instructionToExecute.getMethodInfo();
			int pc = instructionToExecute.getPosition() - 1;
			for (String var : varName) {
				LocalVarInfo localVar = VarSpec.createVarSpec(var).getMatchingLocalVarInfo(mi, pc, slotIdx);
				Integer value = getValue(var);
				if (localVar != null && value != null) {
					this.previousValue = value.compareTo(0) > 0;
				}
			}
		} else {
			this.previousValue = null;
		}
	}

	/**
	 * Returns the value of the given integer variable.
	 * 
	 * @param localVariable the signature of the integer local variable
	 * @return the value of the variable if it is defined, else null.
	 */
	private Integer getValue(String localVariable) {
		StackFrame top = ThreadInfo.getCurrentThread().getTopFrame();
		int index = localVariable.indexOf(':');
		if (top != null && index > 0) {
			MethodSpec methodSpec = MethodSpec.createMethodSpec(localVariable.substring(0, index).trim());
			String varSpec = localVariable.substring(index + 1).trim();
			for (StackFrame frame = top; frame != null; frame = frame.getPrevious()) {
				MethodInfo method = frame.getMethodInfo();
				int slotIdx = frame.getLocalVariableSlotIndex(varSpec);
				if (methodSpec != null && methodSpec.matches(method) && slotIdx >= 0) {
					return frame.getLocalVariable(slotIdx);
				}
			}
		}
		if (localVariable.equals(this.lastModified)) {
			return this.lastValue; // the scope of the variable has ended
		}
		return null;
	}

	private String getSignature(String localVariable) {
		int index = localVariable.indexOf(':');
		String variableName = localVariable.substring(index + 1).trim();

		String signature = null;
		StackFrame top = ThreadInfo.getCurrentThread().getTopFrame();
		if (top != null) {
			MethodSpec methodSpec = MethodSpec.createMethodSpec(localVariable.substring(0, index).trim());
			for (StackFrame frame = top; frame != null; frame = frame.getPrevious()) {
				MethodInfo method = frame.getMethodInfo();
				if (methodSpec != null && methodSpec.matches(method)) {
					signature = method.getClassName().replaceAll("[$.]", "_") + "_" + method.getJNIName();
				}
			}
		}
		if (signature == null && localVariable.equals(this.lastModified)) { // the scope of the variable has ended
			signature = this.lastMethod.getClassName().replaceAll("[$.]", "_") + "_" + this.lastMethod.getJNIName();
		}
		return signature + "__" + variableName;
	}
}
