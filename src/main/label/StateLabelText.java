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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;

/**
 * This listener outputs the labels of the state space to a file, named
 * &lt;name of system under test&gt;.lab, in the format described below. All
 * possible labels are enumerated by positive integers in the first line of the
 * file. Subsequent lines capture the labeled states as follows: the state id
 * followed by a colon and each of its labels, separated by a single space.
 * 
 * @author Syyeda Zainab Fatmi
 */
public class StateLabelText extends StateLabel {
	private StringBuilder result;

	/**
	 * Initializes the listener.
	 * 
	 * @param configuration JPF's configuration
	 */
	public StateLabelText(Config configuration) {
		super(configuration);
		this.result = new StringBuilder();
	}

	/**
	 * Writes the labels for a state to the file.
	 */
	@Override
	public void labelState(int id, Set<Integer> labels) {
		if (!labels.isEmpty()) {
			this.result.append(id + ":");
			for (Integer i : labels) {
				this.result.append(" " + i);
			}
			this.result.append("\n");
		}
	}

	@Override
	public void writeStateLabels(Search search, String name) {
		try {
			PrintWriter writer = new PrintWriter(name + ".lab");
			writer.println(this.enumerateLabels());
			writer.print(this.result);
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Listener could not write to the output file " + name + ".lab");
			search.terminate();
		}
	}

	/**
	 * Enumerates the list of all labels.
	 * 
	 * @return the string of enumerated labels
	 */
	private String enumerateLabels() {
		StringBuilder labelNames = new StringBuilder(); // enumeration of labels
		int n = this.allLabels.size();
		for (int i = 0; i < n; i++) {
			labelNames.append(i + "=\"" + this.allLabels.get(i) + "\" ");
		}
		return labelNames.toString();
	}
}
