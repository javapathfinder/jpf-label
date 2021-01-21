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

import java.util.Set;

import gov.nasa.jpf.vm.Instruction;

/**
 * An interface to define labeling functions which rely on transitions, to be
 * used with the StateLabelText listener.
 * 
 * @author Syyeda Zainab Fatmi
 */
public abstract class TransitionLabelMaker extends StateLabelMaker {

	/**
	 * Whenever an instruction is executed, determines whether to break the current
	 * transition after the executed instruction or not.
	 * 
	 * @param executedInstruction the last instruction that was executed
	 * @return the set of labels for the new state to break the transition, null
	 *         otherwise
	 */
	public Set<Label> breakAfter(Instruction executedInstruction) {
		return null;
	}

	/**
	 * Whenever an instruction is executed, determines whether to break the current
	 * transition before the next instruction or not.
	 * 
	 * @param nextInstruction next instruction which will be executed
	 * @return the set of labels for the new state to break the transition, null
	 *         otherwise
	 */
	public Set<Label> breakBefore(Instruction nextInstruction) {
		return null;
	}

	/**
	 * This method is run whenever JPF's VM is about to execute the next
	 * instruction.
	 * 
	 * @param instructionToExecute The next instruction to be executed
	 */
	public void beforeInstruction(Instruction instructionToExecute) {
	}
}
