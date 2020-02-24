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
import gov.nasa.jpf.jvm.bytecode.IRETURN;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;

/**
 * A labeling function for methods when they return with type boolean.
 * 
 * The methods to be labeled can be specified in the application properties file
 * by setting the property label.ReturnedBooleanMethod. Method signatures must
 * be in the format: package.class.methodName
 * 
 * @author Syyeda Zainab Fatmi
 */
public class ReturnedBooleanMethod extends TransitionLabelMaker {
	private String[] methodName; // method signatures

	/**
	 * Initializes this labeling function.
	 */
	private ReturnedBooleanMethod(Config configuration) {
		methodName = getConfiguredProperty(configuration, "label.ReturnedBooleanMethod.method");
	}

	/**
	 * Creates a ReturnedVoidMethod object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static ReturnedBooleanMethod getInstance(Config configuration) {
		return new ReturnedBooleanMethod(configuration);
	}

	@Override
	public Set<String> breakAfter(Instruction executedInstruction) {
		if (executedInstruction instanceof IRETURN) {
			IRETURN instruction = (IRETURN) executedInstruction;
			MethodInfo methodInfo = instruction.getMethodInfo();
			for (String method : methodName) {
				if (MethodSpec.createMethodSpec(method).matches(methodInfo)) {
					Set<String> labels = new HashSet<String>();
					boolean returnedValue = instruction.getReturnValue() != 0;
					String signature = methodInfo.getClassName().replaceAll("[$.]", "_") + "_"
							+ methodInfo.getJNIName();
					labels.add(returnedValue + "__" + signature);
					return labels;
				}
			}
		}
		return null;
	}
}
