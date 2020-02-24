/*
 * Copyright (C) 2020  Syyeda Zainab Fatmi and Franck van Breugel
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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;

/**
 * This listener produces a dot file representing the state space. The name of
 * the dot file is &lt;name of system under test&gt;.dot.
 * 
 * @author Syyeda Zainab Fatmi
 * @author Franck van Breugel
 */
public class StateLabelDot extends StateLabel {
	private int current; // ID of current state
	private StringBuilder result;

	/**
	 * Initializes this listener.
	 * 
	 * @param configuration JPF's configuration
	 */
	public StateLabelDot(Config configuration) {
		super(configuration);
		this.result = new StringBuilder();
		this.current = -1; // -1 is the ID of the initial state
	}

	/**
	 * Whenever JPF traverses a transition, writes the transition.
	 * 
	 * @param search JPF's search.
	 */
	@Override
	public void stateAdvanced(Search search) {
		super.stateAdvanced(search);
		this.result.append(this.current + " -> " + search.getStateId() + "\n");
		this.current = search.getStateId();
	}

	/**
	 * Whenever JPF backtracks, updates information needed for this listener.
	 * 
	 * @param search JPF's search.
	 */
	@Override
	public void stateBacktracked(Search search) {
		this.current = search.getStateId();
	}

	/**
	 * Whenever JPF restores an earlier visited state, updates information needed
	 * for this listener.
	 * 
	 * @param search JPF's search.
	 */
	@Override
	public void stateRestored(Search search) {
		this.current = search.getStateId();
	}

	/**
	 * Colours a state according to its labels.
	 */
	@Override
	public void labelState(int id, Set<Integer> labels) {
		if (!labels.isEmpty()) {
			Iterator<Integer> iter = labels.iterator();
			if (labels.size() == 1) {
				this.result.append(id + " [style=filled fillcolor=" + getColour(iter.next()) + "]\n");
			} else {
				this.result.append(id + " [fillcolor=\"");
				while (iter.hasNext()) {
					this.result.append(getColour(iter.next()));
					if (iter.hasNext()) {
						this.result.append(":");
					}
				}
				this.result.append("\"]\n");
			}
		}
	}

	@Override
	public void writeStateLabels(Search search, String name) {
		try {
			PrintWriter writer = new PrintWriter(name + ".dot");
			writer.println("digraph statespace {");
			writer.println("node [colorscheme=\"set312\" style=wedged]");
			writer.print(this.result);
			writer.println("}");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			search.terminate();
		}
	}
}
