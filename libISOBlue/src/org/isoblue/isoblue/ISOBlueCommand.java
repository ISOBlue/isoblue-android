package org.isoblue.isoblue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class ISOBlueCommand {

	public enum OpCode {
		FILT, WRITE, MESG,
	}

	private OpCode mOpCode;
	private short mBus;
	private short mSock;
	private byte mData[];

	public static ISOBlueCommand receiveCommand(Scanner s) {
		OpCode opCode = OpCode.MESG;
		short bus;
		short sock;
		String data;

		/* TODO: Parse Opcodes */
		sock = bus = s.nextShort();
		data = s.nextLine();

		return new ISOBlueCommand(opCode, bus, sock, data.substring(0,
				data.length() - 1).getBytes());
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
		String str;

		switch (this.mOpCode) {
		case FILT:
			str = "F";
			break;

		case WRITE:
			str = "W";
			break;

		default:
			str = "";
			break;
		}

		str += " " + this.mBus + " " + new String(this.mData);

		return str;
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
