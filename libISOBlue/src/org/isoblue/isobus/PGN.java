/*
 * Author: Alex Layton <awlayton@purdue.edu>
 *
 * Copyright (c) 2013 Purdue University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.isoblue.isobus;

public class PGN {

	private static final int PF2_MASK = 0x00F000;
	private static final int PS_MASK = 0x0000FF;
	private static final int MAX_VALUE = 0x02FFFF;
	private static final int MIN_VALUE = 0;

	private final int mValue;

	public PGN(int val) throws InvalidPGNException {
		// Check for invalid type
		if ((val & PF2_MASK) != PF2_MASK && (val & PS_MASK) != 0)
			throw new InvalidPGNException(val);

		// Check for value outside allowed range
		if (val < MIN_VALUE || val > MAX_VALUE)
			throw new InvalidPGNException(val);

		mValue = val;
	}

	public int getValue() {
		return mValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(mValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof PGN) && (((PGN) o).getValue() == mValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return mValue;
	}

	public class InvalidPGNException extends IllegalArgumentException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4667515300205962800L;

		public InvalidPGNException(int val) {
			super("Integer \"" + val + "\" is not a valid ISOBUS PGN value");
		}
	}
}
