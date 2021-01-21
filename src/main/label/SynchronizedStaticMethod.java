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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;

/**
 * A labeling function for the locking and unlocking of synchronized methods
 * (that is, the state before the method is invoked and the state after the
 * method returns are labeled).
 * 
 * The methods to be labeled can be specified in the application properties file
 * by setting the property label.SynchronizedStaticMethod. Method signatures
 * must be in the format: package.class.methodName
 * 
 * @author Syyeda Zainab Fatmi
 */
public class SynchronizedStaticMethod extends TransitionLabelMaker {
	private String[] methodName; // method signature

	/**
	 * Initializes this labeling function.
	 */
	private SynchronizedStaticMethod(Config configuration) {
		methodName = getConfiguredProperty(configuration, "label.SynchronizedStaticMethod.method");
	}

	/**
	 * Creates a SynchronizedStaticMethod object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static SynchronizedStaticMethod getInstance(Config configuration) {
		return new SynchronizedStaticMethod(configuration);
	}

	@Override
	public Set<Label> breakAfter(Instruction executedInstruction) {
		if (executedInstruction instanceof ReturnInstruction) {
			ReturnInstruction instruction = (ReturnInstruction) executedInstruction;
			MethodInfo methodInfo = instruction.getMethodInfo();
			if (Modifier.isSynchronized(methodInfo.getModifiers()) && Modifier.isStatic(methodInfo.getModifiers())) {
				for (String method : methodName) {
					if (MethodSpec.createMethodSpec(method).matches(methodInfo)) {
						Set<Label> labels = new HashSet<Label>();
						String signature = methodInfo.getClassName().replaceAll("[$.]", "_") + "_"
								+ methodInfo.getJNIName();
						labels.add(new Label("unlocked__" + signature, method + " unlocked"));
						return labels;
					}
				}
			}
		}
		return null;
	}

	@Override
	public Set<Label> breakBefore(Instruction nextInstruction) {
		if (nextInstruction instanceof INVOKESTATIC) {
			JVMInvokeInstruction instruction = (JVMInvokeInstruction) nextInstruction;
			MethodInfo methodInfo = instruction.getInvokedMethod();
			// 32 represents the modifier 'synchronized'
			if (Modifier.isSynchronized(methodInfo.getModifiers())) {
				for (String method : methodName) {
					if (MethodSpec.createMethodSpec(method).matches(methodInfo)) {
						Set<Label> labels = new HashSet<Label>();
						String signature = methodInfo.getClassName().replaceAll("[$.]", "_") + "_"
								+ methodInfo.getJNIName();
						labels.add(new Label("locked__" + signature, method + " locked"));
						return labels;
					}
				}
			}
		}
		return null;
	}
}