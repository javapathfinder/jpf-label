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
import java.util.Random;

/**
 * A sample app that illustrates the use of the labelling class label.BooleanStaticField.field.
 *
 * @author Syyeda Zainab Fatmi
 */
public class MultipleFields {
    private static boolean one = true;
    private static boolean two = true;
    private static boolean three = false;

    public static void main(String[] args) {
	Random random = new Random();
	if (random.nextBoolean()) {
	    MultipleFields.three = false;
	    MultipleFields.two = true;
	} else {
	    MultipleFields.three = true;
	    MultipleFields.two = false;
	}
    }
}
