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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.VMListener;

/**
 * An abstract listener which formats the labelling of the states.
 * 
 * The classes implementing StateLabelMaker or TransitionLabelMaker, defining
 * the labeling functions, can be specified in the application properties file
 * by setting the property label.class.
 * 
 * @author Syyeda Zainab Fatmi
 */
public abstract class StateLabel extends ListenerAdapter implements SearchListener, VMListener {
	protected List<Label> allLabels; // string representation of all possible labels
	private List<StateLabelMaker> labelMakers; // registered label makers
	private Set<Integer> currentStateLabels; // labels for the current state
	private int states; // number of states

	/**
	 * Initializes this listener.
	 * 
	 * @param configuration JPF's configuration
	 */
	public StateLabel(Config configuration) {
		this.allLabels = new ArrayList<Label>();
		this.labelMakers = new ArrayList<StateLabelMaker>();
		this.currentStateLabels = new TreeSet<Integer>();
		this.states = 0;

		String[] classes = configuration.getCompactTrimmedStringArray("label.class");
		for (String name : classes) {
			try {
				Class clazz = Class.forName(name);
				Method method = clazz.getDeclaredMethod("getInstance", gov.nasa.jpf.Config.class);
				Object result = method.invoke(null, configuration); // result of invoking
																	// name.getInstance(configuration)
				this.labelMakers.add((StateLabelMaker) result);
			} catch (Exception e) {
				System.out.println("Class " + name + " could not be instantiated");
				e.printStackTrace();
			}
		}
	}

	/**
	 * When JPF starts, adds the labels of the initial state.
	 * 
	 * @param search JPF's search.
	 */
	@Override
	public void searchStarted(Search search) {
		this.getStateLabels(search);
		this.labelState(-1, this.currentStateLabels);
	}

	/**
	 * Whenever JPF reaches a new state, adds the labels of that state.
	 * 
	 * @param search JPF's search
	 */
	@Override
	public void stateAdvanced(Search search) {
		if (search.isNewState()) {
			int stateID = search.getStateId(); // current state
			this.getStateLabels(search);
			this.labelState(stateID, this.currentStateLabels);
			this.states = Math.max(stateID, this.states);
		}
	}

	/**
	 * Dumps the state space if a constraint is hit.
	 * 
	 * @param search JPF's search
	 */
	@Override
	public void searchConstraintHit(Search search) {
		this.writeStateLabels(search, search.getVM().getSUTName() + "_" + search.getSearchConstraint());
	}

	/**
	 * When JPF finishes, writes the labelling to a file.
	 * 
	 * @param search JPF's search.
	 */
	@Override
	public void searchFinished(Search search) {
		this.writeStateLabels(search, search.getVM().getSUTName());
	}

	/**
	 * Formats the labelling of the given state with the given set of labels.
	 * 
	 * @param id     the id of the state
	 * @param labels the set of indices of the labels
	 */
	public abstract void labelState(int id, Set<Integer> labels);

	/**
	 * Writes the current labelling of the state space to a file.
	 * 
	 * @param search JPF's search
	 * @param name   the name of the system under test, appended with the search
	 *               constraint if one was hit.
	 */
	public abstract void writeStateLabels(Search search, String name);

	/**
	 * Whenever an instruction is executed, breaks the transition if any of the
	 * registered label makers signals to do so.
	 * 
	 * @param vm                  JPF's virtual machine
	 * @param currentThread       the current thread
	 * @param nextInstruction     the next instruction to be executed
	 * @param executedInstruction the last execution that was executed
	 */
	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
			Instruction executedInstruction) {
		boolean b = false; // should the transition be broken?
		this.currentStateLabels = new TreeSet<Integer>(); // labels for the new state
		for (StateLabelMaker labelMaker : this.labelMakers) {
			if (labelMaker instanceof TransitionLabelMaker) {
				Set<Label> labels = ((TransitionLabelMaker) labelMaker).breakAfter(executedInstruction);
				b |= addLabelIndices(labels);
				labels = ((TransitionLabelMaker) labelMaker).breakBefore(nextInstruction);
				b |= addLabelIndices(labels);
			}
		}
		if (b) {
			vm.breakTransition("Instruction executed");
		}
	}

	/**
	 * Whenever VM is about to execute the next instruction, allows the registered
	 * label makers to obtain any required information.
	 * 
	 * @param vm                   JPF's virtual machine
	 * @param currentThread        the current thread
	 * @param instructionToExecute the next instruction to be executed
	 */
	@Override
	public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
		for (StateLabelMaker lm : labelMakers) {
			if (lm instanceof TransitionLabelMaker) {
				TransitionLabelMaker tlm = (TransitionLabelMaker) lm;
				tlm.beforeInstruction(instructionToExecute);
			}
		}
	}

	/**
	 * Returns the colour palette/scheme for the label with given index.
	 * 
	 * @param i the index of the label.
	 * @return the hexadecimal representation of the colour associated with that
	 *         label.
	 */
	protected String getColour(int i) {
		if (i < 12) {
			// use a colour scheme for the first 12 colours
			return Integer.toString(i + 1);
		} else {
			i -= 12;
			// Reference: https://krazydad.com/tutorials/makecolors.php
			String r = Integer.toHexString((int) (Math.sin(2.4 * i + 0) * 127 + 128));
			if (r.length() == 1)
				r = "0" + r;
			String g = Integer.toHexString((int) (Math.sin(2.4 * i + 2) * 127 + 128));
			if (g.length() == 1)
				g = "0" + g;
			String b = Integer.toHexString((int) (Math.sin(2.4 * i + 4) * 127 + 128));
			if (b.length() == 1)
				b = "0" + b;
			return "#" + r + g + b;
		}
	}

	/**
	 * Produces a dot file with a legend mapping each colour to the description of
	 * the label it represents. The name of the dot file is &lt;name of system under
	 * test&gt;_legend.dot.
	 */
	protected void generateLegendFile() {
		String name = VM.getVM().getSUTName() + "_legend.dot";
		try {
			PrintWriter writer = new PrintWriter(name);
			writer.println("digraph legend {");
			writer.println("node [colorscheme=\"set312\" shape=plaintext]");
			writer.println("{ legend_node [");
			writer.println("label=<");
			writer.println("<table border=\"0\" cellborder=\"0\" cellspacing=\"0\">");
			writer.println("<tr><td colspan=\"2\">Legend</td></tr>");
			int n = this.allLabels.size();
			for (int i = 0; i < n; i++) {
				writer.println("<tr><td width=\"35\" bgcolor=\"" + this.getColour(i) + "\"></td><td align=\"left\">"
						+ this.allLabels.get(i).getDescription() + "</td></tr>");
			}
			writer.println("</table>>");
			writer.println("];}");
			writer.println("}");
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Listener could not write to the legend file " + name);
			e.printStackTrace();
		}
	}

	/**
	 * Obtains the the labels for the current state of JPF's search from the label
	 * makers and adds their indices to the set of labels for the current state.
	 * 
	 * @param search JPF's search
	 */
	private void getStateLabels(Search search) {
		for (StateLabelMaker labelMaker : this.labelMakers) {
			addLabelIndices(labelMaker.getStateLabels(search));
		}
	}

	/**
	 * Takes the given set of labels and adds their indices to the set of labels for
	 * the current state.
	 * 
	 * @param labels the set of labels
	 * @return false if the given set was null, true otherwise
	 */
	private boolean addLabelIndices(Set<Label> labels) {
		if (labels != null) {
			for (Label label : labels) {
				if (!this.allLabels.contains(label)) {
					this.allLabels.add(label);
				}
				this.currentStateLabels.add(this.allLabels.indexOf(label));
			}
			return true;
		}
		return false;
	}
}
