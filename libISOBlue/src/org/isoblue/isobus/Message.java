package org.isoblue.isobus;

public class Message {

	// private final NAME mSrcName, mDestName;
	private final short mSrcAddr, mDestAddr;
	private final PGN mPgn;
	private final byte mData[];
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
		String str;

		str = "PGN:" + mPgn.toString() + " SA:" + mSrcAddr + " DA:" + mDestAddr
				+ " Data:";
		for (byte b : mData) {
			str += String.format("%02x", b);
		}

		return str;
	}
}
