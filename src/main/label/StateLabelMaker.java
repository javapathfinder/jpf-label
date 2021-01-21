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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;

/**
 * An interface to define labeling functions to be used with the StateLabelText
 * listener.
 * 
 * @author Syyeda Zainab Fatmi
 */
public abstract class StateLabelMaker {

	/**
	 * Static method to instantiate the class. The getInstance() method must be
	 * defined in extending classes.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of StateLabelMaker
	 */
	public static StateLabelMaker getInstance(Config configuration) {
		System.out.println("The getInstance() method was not implemented in the subclass.");
		return null;
	}

	/**
	 * Whenever the search advances to the next state, returns the labels associated
	 * with the new state.
	 * 
	 * @param search JPF's search
	 * @return an array of labels for the current state
	 */
	public Set<Label> getStateLabels(Search search) {
		return null;
	}

	/**
	 * Finds the value of the property with the specified key. The value is then
	 * split with the delimiter ';' and an array is returned.
	 * 
	 * @param configuration JPF's Config
	 * @param key           the property key
	 * @return an array of values of the property with the specified key, if the
	 *         property was defined, otherwise returns an empty array
	 */
	protected String[] getConfiguredProperty(Config configuration, String key) {
		char[] delimiter = { ';' };
		String[] property = configuration.getStringArray(key, delimiter);
		if (property != null) {
			for (int i = 0; i < property.length; i++) {
				String s = property[i];
				if (s != null && s.length() > 0) {
					property[i] = s.trim();
				}
			}
			return Config.removeEmptyStrings(property);
		} else {
			return new String[0];
		}
	}
}
