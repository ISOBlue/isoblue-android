/*
 * Author: Alex Layton <alex@layton.in>
 * 
 * Copyright (c) 2013 Purdue University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.isoblue.isobus;

import java.io.Serializable;

/**
 * Represents and ISOBUS Parameter Group Number (PGN). A {@link PGN} is used to
 * identify and interpret the data within a {@link Message}.
 * 
 * @see Message
 * @author Alex Layton <alex@layton.in>
 */
public final class PGN implements Serializable {

    private static final long serialVersionUID = 1324435753164108638L;

    private static final int PF2_MASK = 0x00F000;

    private static final int PS_MASK = 0x0000FF;

    /**
     * Constant for the minimum {@code int} corresponding to a valid ISOBUS PGN
     */
    private static final int MAX_VALUE = 0x02FFFF;

    /**
     * Constant for the minimum {@code int} corresponding to a valid ISOBUS PGN
     */
    private static final int MIN_VALUE = 0;

    /**
     * The {@code int} representation of this {@code PGN}.
     */
    private final int mInt;

    /**
     * Construct a new {@link PGN} from the specified {@code int}
     * representation.
     * <p class="note">
     * <b>Note:</b> Many {@code int} values do not correspond to valid ISOBUS
     * PGNs.
     * 
     * @param intRep
     *            the {@code int} representation
     * @throws InvalidPGNException
     *             if {@code intRep} does not represent a valid ISOBUS PGN
     */
    public PGN(int intRep) throws InvalidPGNException {
        // Check for invalid type
        if ((intRep & PF2_MASK) != PF2_MASK && (intRep & PS_MASK) != 0)
            throw new InvalidPGNException(intRep);

        // Check for value outside allowed range
        if (intRep < MIN_VALUE || intRep > MAX_VALUE)
            throw new InvalidPGNException(intRep);

        mInt = intRep;
    }

    /**
     * Get the {@code int} representation of this {@link PGN}.
     * 
     * @return the {@code int} representation
     */
    public int asInt() {
        return mInt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PGN:" + Integer.toString(mInt);
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code PGN} and have the same integer representation as this object.
     * 
     * @param o
     *            the object to compare this {@link PGN} with
     * @return {@code true} if the specified object is equal to this
     *         {@code Integer}; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof PGN) && (((PGN) o).mInt == mInt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return mInt;
    }

    /**
     * Thrown when an {@code int} not representing a valid ISOBUS PGN is used to
     * construct a {@link PGN}.
     * 
     * @author Alex Layton <alex@layton.in>
     */
    public final class InvalidPGNException extends IllegalArgumentException {

        private static final long serialVersionUID = 4667515300205962800L;

        /**
         * Constructs a new {@link InvalidPGNException} with the current stack
         * trace and a detail message mentioning {@code intRep}.
         * 
         * @param intRep
         *            the {@code int} which does not represent a valid ISOBUS
         *            PGN
         */
        private InvalidPGNException(int intRep) {
            super("Integer \"" + intRep
                    + "\" does not represent a valid ISOBUS PGN");
        }
    }
}
