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
import gov.nasa.jpf.search.Search;

/**
 * A labeling function that labels each state with a different label.
 * 
 * @author Syyeda Zainab Fatmi
 */
public class AllDifferent extends StateLabelMaker {
	private int label;

	/**
	 * Initializes this labeling function.
	 */
	private AllDifferent() {
		this.label = 0;
	}

	/**
	 * Creates a AllDifferent object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static AllDifferent getInstance(Config configuration) {
		return new AllDifferent();
	}

	@Override
	public Set<String> getStateLabels(Search search) {
		Set<String> labels = new HashSet<String>();
		labels.add("_" + Integer.toString(label++));
		return labels;
	}
}
