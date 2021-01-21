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
 * A labeling function for initial states.
 * 
 * @author Syyeda Zainab Fatmi
 */
public class Initial extends StateLabelMaker {
	private boolean init; // true for the first state, false otherwise

	/**
	 * Initializes this labeling function.
	 */
	private Initial() {
		this.init = true;
	}

	/**
	 * Creates an Initial object.
	 * 
	 * @param configuration JPF's configuration
	 * @return an instance of this class
	 */
	public static Initial getInstance(Config configuration) {
		return new Initial();
	}

	@Override
	public Set<Label> getStateLabels(Search search) {
		Set<Label> labels = new HashSet<Label>();
		if (this.init) {
			this.init = false;
			labels.add(new Label("init", "initial"));
		}
		return labels;
	}
}
