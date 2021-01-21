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
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Types;

/**
 * A labeling function for methods when they are invoked.
 * 
 * The methods to be labeled can be specified in the application properties file
 * by setting the property label.InvokedMethod. Method signatures must be in the
 * format: package.class.methodName
 * 
 * @author Syyeda Zainab Fatmi
 */
public class InvokedMethod extends TransitionLabelMaker {
	private String[] methodName; // method signatures

	/**
	 * Initializes this labeling function.
	 */
	private InvokedMethod(Config configuration) {
		methodName = getConfiguredProperty(configuration, "label.InvokedMethod.method");
	}

	/**
	 * Creates an InvokedMethod object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static InvokedMethod getInstance(Config configuration) {
		return new InvokedMethod(configuration);
	}

	@Override
	public Set<Label> breakBefore(Instruction nextInstruction) {
		if (nextInstruction instanceof INVOKESTATIC) {
			INVOKESTATIC instruction = (INVOKESTATIC) nextInstruction;
			MethodInfo methodInfo = instruction.getInvokedMethod();
			for (String method : methodName) {
				if (MethodSpec.createMethodSpec(method).matches(methodInfo)) {
					Set<Label> labels = new HashSet<Label>();
					String signature = methodInfo.getClassName().replaceAll("[$.]", "_") + "_"
							+ methodInfo.getJNIName();
					labels.add(new Label("invoked__" + signature, method + " is invoked"));
					return labels;
				}
			}
		} else if (nextInstruction instanceof InstanceInvocation) {
			InstanceInvocation instruction = (InstanceInvocation) nextInstruction;
			// The MethodInfo object is not yet initialized for an instance invocation
			String invokedClass = instruction.getInvokedMethodClassName();
			String invokedMethod = instruction.getInvokedMethodName();
			invokedMethod = invokedMethod.split("\\(", 2)[0]; // remove the signature part
			for (String method : methodName) {
				if (MethodSpec.createMethodSpec(method).matches(invokedClass, invokedMethod)) {
					Set<Label> labels = new HashSet<Label>();
					String signature = invokedClass.replaceAll("[$.]", "_") + "_" + Types.getJNIMangledMethodName(null,
							invokedMethod, instruction.getInvokedMethodSignature());
					labels.add(new Label("invoked__" + signature, method + " is invoked"));
					return labels;
				}
			}
		}
		return null;
	}
}
