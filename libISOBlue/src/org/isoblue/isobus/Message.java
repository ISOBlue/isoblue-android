/*
 * Author: Alex Layton <alex@layton.in>
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

public class Message {

	// private final NAME mSrcName, mDestName;
	private final short mSrcAddr, mDestAddr;
	private final PGN mPgn;
	private final byte mData[];

	/**
	 * Times since the epoch, in microseconds
	 */
	private final long mTimeStamp;

	public Message(short destAddr, PGN pgn, byte data[]) {
		this(destAddr, (short) -1, pgn, data, -1);
	}

	public Message(short destAddr, short srcAddr, PGN pgn, byte data[],
			long timeStamp) {
		// TODO: Use NAMEs instead of addresses for source/destination
		mDestAddr = destAddr;
		mSrcAddr = srcAddr;
		mPgn = pgn;
		mData = data;
		mTimeStamp = timeStamp;
	}

	/**
	 * @return the mSrcAddr
	 */
	public short getSrcAddr() {
		return mSrcAddr;
	}

	/**
	 * @return the mDestAddr
	 */
	public short getDestAddr() {
		return mDestAddr;
	}

	/**
	 * @return the mPgn
	 */
	public PGN getPgn() {
		return mPgn;
	}

	/**
	 * @return the mData
	 */
	public byte[] getData() {
		return mData;
	}

	/**
	 * @return the mTimeStamp
	 */
	public long getTimeStamp() {
		return mTimeStamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("PGN:").append(mPgn);
		s.append(" SA:").append(mSrcAddr);
		s.append(" DA:").append(mDestAddr);

		s.append(" Data:");
		for (byte b : mData) {
			int val;

			// Put byte's value into and int, treating byte as unsigned
			val = b & 0x7f;
			val += (b & 0x80);
			s.append(" ");

			if (val < 0x10)
				s.append("0");
			s.append(Integer.toString(val, 16));
		}

		s.append(" Time: ").append(mTimeStamp);

		return s.toString();
	}
}
