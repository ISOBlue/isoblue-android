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

package org.isoblue.isoblue;

import java.io.IOException;
import java.io.OutputStream;

public class ISOBlueCommand {

	public enum OpCode {
		FILT, WRITE, MESG,
	}

	private OpCode mOpCode;
	private short mBus;
	private short mSock;
	private byte mData[];

	public static ISOBlueCommand receiveCommand(String line) {
		OpCode opCode = OpCode.MESG;
		short bus;
		short sock;
		String data;

		/* TODO: Parse Opcodes */
		sock = bus = (short) Integer.parseInt(line.substring(0, 1), 16);
		data = line.substring(1, line.length());

		return new ISOBlueCommand(opCode, bus, sock, data.getBytes());
	}

	public void sendCommand(OutputStream os) throws IOException {
		os.write((this.toString() + "\n").getBytes());
	}

	public ISOBlueCommand(OpCode opCode) {
		this(opCode, (short) -1, (short) -1, new byte[0]);
	}

	public ISOBlueCommand(OpCode opCode, short bus, short sock, byte data[]) {
		mOpCode = opCode;
		mBus = bus;
		mSock = sock;

		mData = new byte[data.length];
		System.arraycopy(data, 0, mData, 0, data.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder s;
		
		s = new StringBuilder();

		switch (this.mOpCode) {
		case FILT:
			s.append("F");
			break;

		case WRITE:
			s.append("W");
			break;

		default:
			break;
		}

		s.append(String.format("%1x", this.mBus));
		s.append(new String(this.mData));

		return s.toString();
	}

	/**
	 * @return the mOpCode
	 */
	public OpCode getOpCode() {
		return mOpCode;
	}

	/**
	 * @return the mBus
	 */
	public short getBus() {
		return mBus;
	}

	/**
	 * @return the mSock
	 */
	public short getSock() {
		return mSock;
	}

	/**
	 * @return the Data
	 */
	public byte[] getData() {
		return mData;
	}
}
