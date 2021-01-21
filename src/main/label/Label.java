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

/**
 * This class represents a state label.
 * 
 * @author Syyeda Zainab Fatmi
 */
public class Label {
	private String name;
	private String description;

	/**
	 * Creates a state label.
	 * 
	 * @param name        the name of the label in PRISM's labelling format
	 * @param description a short description of the label in HTML text format
	 */
	public Label(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Returns the name of this label.
	 * 
	 * @return the name of the label
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a short description of this label.
	 * 
	 * @return the description of the label
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object != null && object instanceof Label) {
			Label other = (Label) object;
			if (this.name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!this.name.equals(other.name)) {
				return false;
			}
			if (this.description == null) {
				if (other.description != null) {
					return false;
				}
			} else if (!this.description.equals(other.description)) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
}
