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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

/**
 * Tests the StateLabelText and StateLabelDot listeners.
 * 
 * @author Syyeda Zainab Fatmi
 */
public class StateLabelTest extends TestJPF {

	/**
	 * Private static inner class for the fields and methods monitored/labelled in
	 * the test cases.
	 */
	private static class Tester {

		/** Field used in staticBooleanFieldTest */
		private static boolean condition;

		/** Field used in multipleTest */
		private static boolean x;

		/** Field used in other tests */
		private static boolean attribute;

		/** Method used in invokedMethodTest and multipleTest */
		public static void m() {
			x = true;
		}

		/** Method used in returnedMethodTest and multipleTest */
		public static void n() {
			x = true;
		}

		/** Method used in synchronizedStaticMethodTest */
		public static synchronized void s() {
			x = true;
		}
	}

	/**
	 * JPF's application properties for testing a single label-maker.
	 */
	private static String[] singleLabelMakerProperties = { "+cg.enumerate_random=true",
			"+listener=label.StateLabelText;label.StateLabelDot", "", "" };

	/**
	 * JPF's application properties used in multipleTest and fieldAndVarTest.
	 */
	private static String[] multipleLabelMakerProperties = { "+cg.enumerate_random=true",
			"+listener=label.StateLabelText;label.StateLabelDot",
			"+label.class = label.AllDifferent; label.Initial; label.End;"
					+ "label.InvokedStaticMethod; label.PositiveIntegerLocalVariable;"
					+ "label.ReturnedVoidMethod; label.BooleanStaticField",
			"+label.BooleanStaticField.field = label.StateLabelTest$Tester.x; label.StateLabelTest$Tester.condition",
			"+label.InvokedStaticMethod.method = label.StateLabelTest$Tester.m(); label.StateLabelTest$Tester.n()",
			"+label.ReturnedVoidMethod.method = label.StateLabelTest$Tester.m(); label.StateLabelTest$Tester.n()",
			"+label.PositiveIntegerLocalVariable.variable = label.StateLabelTest.multipleTest():y;"
					+ "label.StateLabelTest.fieldAndVarTest():a; label.StateLabelTest.fieldAndVarTest():b" };

	/**
	 * The path to the directory containing the test files/resources.
	 */
	private static String path;

	/**
	 * The name of the label file (i.e. the default file name is the signature of
	 * the target class).
	 */
	private static String labelFileName = StateLabelTest.class.getName() + ".lab";

	/**
	 * The name of the graph file (i.e. the default file name is the signature of
	 * the target class).
	 */
	private static String dottyFileName = StateLabelTest.class.getName() + ".dot";

	/**
	 * Defines the paths.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		path = JPF.createConfig(new String[] {}).getString("jpf-label") + "/src/tests/resources/";
	}

	/**
	 * Removes the label file created in the test cases.
	 */
	@AfterClass
	public static void tearDownAfterClass() {
		File labelFile = new File(labelFileName);
		labelFile.delete();

		File dottyFile = new File(dottyFileName);
		dottyFile.delete();
	}

	/**
	 * Tests the listeners with no labeling classes.
	 */
	@Test
	public void emptyTest() {
		singleLabelMakerProperties[2] = "";
		singleLabelMakerProperties[3] = "";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			// do nothing
		} else {
			long length = new File(labelFileName).length();
			assertTrue("One or two lines expected, " + length + " lines found", length == 1 || length == 2); // one or two new lines
			assertTrue(filesEqual(dottyFileName, path + "empty.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the initial state.
	 */
	@Test
	public void initialTest() {
		singleLabelMakerProperties[2] = "+label.class=label.Initial";
		singleLabelMakerProperties[3] = "";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "initial.lab"));
			assertTrue(filesEqual(dottyFileName, path + "initial.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the final states.
	 */
	@Test
	public void finalTest() {
		singleLabelMakerProperties[2] = "+label.class=label.End";
		singleLabelMakerProperties[3] = "";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "end.lab"));
			assertTrue(filesEqual(dottyFileName, path + "end.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling each state with a different character.
	 */
	@Test
	public void allDifferentTest() {
		singleLabelMakerProperties[2] = "+label.class=label.AllDifferent";
		singleLabelMakerProperties[3] = "";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "allDifferent.lab"));
			assertTrue(filesEqual(dottyFileName, path + "allDifferent.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the static boolean attribute "condition".
	 */
	@Test
	public void booleanStaticFieldTest() {
		singleLabelMakerProperties[2] = "+label.class=label.BooleanStaticField";
		singleLabelMakerProperties[3] = "+label.BooleanStaticField.field = label.StateLabelTest$Tester.condition";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			int i = 0;
			Tester.condition = true;
			Random random = new Random();
			while (Tester.condition && Math.abs(i) < 5) {
				if (random.nextBoolean()) {
					i++;
				} else {
					i--;
					Tester.condition = !Tester.condition;
				}
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "booleanStaticField.lab"));
			assertTrue(filesEqual(dottyFileName, path + "booleanStaticField.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling local variables.
	 */
	@Test
	public void positiveIntegerLocalVariableTest() {
		singleLabelMakerProperties[2] = "+label.class=label.PositiveIntegerLocalVariable";
		singleLabelMakerProperties[3] = "+label.PositiveIntegerLocalVariable.variable = label.StateLabelTest.positiveIntegerLocalVariableTest():variable";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			int variable = 0;
			variable++; // positive
			variable -= 3;
			variable--;
			variable = 5; // positive
			variable /= 2; // positive
			variable = (int) -12.0;
			variable *= 2;
			variable += 92; // positive
			variable = variable - 110;
			variable = 14;
		} else {
			assertTrue(filesEqual(labelFileName, path + "positiveIntegerLocalVariable.lab"));
			assertTrue(filesEqual(dottyFileName, path + "positiveIntegerLocalVariable.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the invoke of a static method.
	 */
	@Test
	public void invokedStaticMethodTest() {
		singleLabelMakerProperties[2] = "+label.class=label.InvokedStaticMethod";
		singleLabelMakerProperties[3] = "+label.InvokedStaticMethod.method = label.StateLabelTest$Tester.m()";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
			if (Tester.attribute) {
				Tester.m();
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "invokedStaticMethod.lab"));
			assertTrue(filesEqual(dottyFileName, path + "staticMethod.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the return of a static method.
	 */
	@Test
	public void returnedVoidMethodTest() {
		singleLabelMakerProperties[2] = "+label.class=label.ReturnedVoidMethod";
		singleLabelMakerProperties[3] = "+label.ReturnedVoidMethod.method = label.StateLabelTest$Tester.n()";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
			if (Tester.attribute) {
				Tester.n();
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "returnedVoidMethod.lab"));
			assertTrue(filesEqual(dottyFileName, path + "staticMethod.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling the locking and unlocking of a synchronized
	 * method.
	 */
	@Test
	public void synchronizedStaticMethodTest() {
		singleLabelMakerProperties[2] = "+label.class=label.SynchronizedStaticMethod";
		singleLabelMakerProperties[3] = "+label.SynchronizedStaticMethod.method = label.StateLabelTest$Tester.s()";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.attribute = false;
			}
			if (Tester.attribute) {
				Tester.s();
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "synchronizedStaticMethod.lab"));
			assertTrue(filesEqual(dottyFileName, path + "synchronizedStaticMethod.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling thrown exceptions and errors.
	 */
	@Test
	public void throwableTest() {
		singleLabelMakerProperties[2] = "+label.class=label.ThrownException";
		singleLabelMakerProperties[3] = "+label.ThrownException.type = java.io.FileNotFoundException;"
				+ "java.lang.IllegalArgumentException;java.lang.AssertionError";

		if (verifyNoPropertyViolation(singleLabelMakerProperties)) {
			try {
				throw new AssertionError();
			} catch (Error e) {
				System.out.println(e.toString());
			}
			try {
				throw new FileNotFoundException("Message");
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			try {
				throw new IllegalArgumentException();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "throwable.lab"));
			assertTrue(filesEqual(dottyFileName, path + "throwable.dot"));
		}
	}

	/**
	 * Tests the listeners with multiple labeling functions.
	 */
	@Test
	public void multipleTest() {
		if (verifyNoPropertyViolation(multipleLabelMakerProperties)) {
			Tester.x = false;
			if (!Tester.x) {
				Tester.x = false;
			}
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.m();
			} else {
				Tester.n();
			}
			int y = 5;
			if (y > 3) {
				Tester.x = false;
			}
			Tester.x = true;
			Tester.attribute = true;
		} else {
			assertTrue(filesEqual(labelFileName, path + "multiple.lab"));
			assertTrue(filesEqual(dottyFileName, path + "multiple.dot"));
		}
	}

	/**
	 * Tests the listeners with labeling multiple fields and variables, as well as
	 * initial and final states and different labels for each state.
	 */
	@Test
	public void fieldAndVarTest() {
		if (verifyNoPropertyViolation(multipleLabelMakerProperties)) {
			Tester.x = false;
			Tester.condition = false;
			Tester.attribute = false; // not labeled
			int a = 1;
			int b = -5;
			int c = -2; // not labeled

			Tester.x = false;
			a = -4;
			for (int i = 0; i < 12; i++) {
				b++;
			}
			Random random = new Random();
			if (random.nextBoolean()) {
				Tester.attribute = true;
			} else {
				Tester.condition = true;
				Tester.x = true;
			}
		} else {
			assertTrue(filesEqual(labelFileName, path + "fieldAndVar.lab"));
			assertTrue(filesEqual(dottyFileName, path + "fieldAndVar.dot"));
		}
	}

	/**
	 * Compares two files and determines whether have the same contents.
	 * 
	 * @param actual   Name of the actual file.
	 * @param expected Name of the expected file.
	 * @return true if the files have the same contents, false otherwise.
	 */
	private boolean filesEqual(String actual, String expected) {
		try {
			Scanner actualFile = new Scanner(new File(actual));
			Scanner expectedFile = new Scanner(new File(expected));
			while (expectedFile.hasNextLine()) {
				if (!actualFile.hasNextLine() || !actualFile.nextLine().equals(expectedFile.nextLine())) {
					actualFile.close();
					expectedFile.close();
					return false;
				}
			}
			actualFile.close();
			expectedFile.close();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("The file could not be read.");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Runs the test methods with the given names. If no names are given, all test
	 * methods are run.
	 *
	 * @param testMethods the names of the test methods to be run.
	 */
	public static void main(String[] testMethods) {
		runTestsOfThisClass(testMethods);
	}
}
