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
