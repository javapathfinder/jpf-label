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
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.FieldSpec;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;

/**
 * A labeling function for a static boolean attribute.
 * 
 * The fields to be labeled can be specified in the application properties file
 * by setting the property label.BooleanStaticField. Field signatures must be in
 * the format: package.class.fieldName
 * 
 * @author Syyeda Zainab Fatmi
 */
public class BooleanStaticField extends TransitionLabelMaker {
	private String[] fieldName; // field signature
	private Boolean previousValue;

	/**
	 * Initializes this labeling function.
	 */
	private BooleanStaticField(Config configuration) {
		fieldName = getConfiguredProperty(configuration, "label.BooleanStaticField.field");
		previousValue = null;
	}

	/**
	 * Creates a BooleanStaticField object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static BooleanStaticField getInstance(Config configuration) {
		return new BooleanStaticField(configuration);
	}

	@Override
	public Set<Label> getStateLabels(Search search) {
		Set<Label> labels = new HashSet<Label>();
		for (String field : fieldName) {
			Boolean value = getValue(field);
			if (value != null) {
				labels.add(new Label(value + "__" + field.replaceAll("[$.]", "_"), field + " = " + value));
			}
		}
		return labels;
	}

	@Override
	public Set<Label> breakAfter(Instruction executedInstruction) {
		// static attributes are set in PUTSTATIC instructions
		if (executedInstruction instanceof PUTSTATIC) {
			PUTSTATIC instruction = (PUTSTATIC) executedInstruction;
			FieldInfo fieldInfo = instruction.getFieldInfo();
			// if the instruction modifies an attribute of interest, break the transition
			for (String field : fieldName) {
				if (FieldSpec.createFieldSpec(field).matches(fieldInfo) && !getValue(field).equals(previousValue)) {
					return new HashSet<Label>();
				}
			}
		}
		return null;
	}

	@Override
	public void beforeInstruction(Instruction instructionToExecute) {
		if (instructionToExecute instanceof PUTSTATIC) {
			PUTSTATIC instruction = (PUTSTATIC) instructionToExecute;
			FieldInfo fieldInfo = instruction.getFieldInfo();
			for (String field : fieldName) {
				if (FieldSpec.createFieldSpec(field).matches(fieldInfo)) {
					previousValue = getValue(field);
				}
			}
		}
	}

	/**
	 * Returns the value of the given static boolean field.
	 * 
	 * @param field the signature of the static boolean field
	 * @return the value of the field if the class is resolved, else null.
	 */
	private Boolean getValue(String fieldSignature) {
		ClassLoaderInfo loader = ClassLoaderInfo.getCurrentClassLoader();
		int index = fieldSignature.lastIndexOf('.');
		if (loader != null && index > 0) {
			ClassInfo clazz = loader.tryGetResolvedClassInfo(fieldSignature.substring(0, index).trim());
			if (clazz != null) {
				FieldInfo field = clazz.getStaticField(fieldSignature.substring(index + 1).trim());
				ElementInfo element = clazz.getStaticElementInfo();
				if (element != null && field != null && field.isBooleanField()) {
					return element.getBooleanField(field);
				}
			}
		}
		return null;
	}
}
