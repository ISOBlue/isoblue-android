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
		String[] tokens;
		OpCode opCode = OpCode.MESG;
		short bus;
		short sock;
		String data;

		tokens = line.split(" ", 2);
		/* TODO: Parse Opcodes */
		sock = bus = Short.parseShort(tokens[0]);
		data = tokens[1];

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

		s.append(" ").append(this.mBus);
		s.append(" ").append(new String(this.mData));

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
