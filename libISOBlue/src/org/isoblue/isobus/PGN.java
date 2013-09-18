package org.isoblue.isobus;

public class PGN {

	private int mPgn;

	public PGN(int pgn) throws InvalidPGNException {
		if (pgn < 240 && (pgn & 0x0FF) != 0)
			throw new InvalidPGNException();

		mPgn = pgn;
	}

	public int asInt() {
		return mPgn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(mPgn);
	}

	public class InvalidPGNException extends IllegalArgumentException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4667515300205962800L;

		public InvalidPGNException() {
			super("Given integer is not a valid ISOBUS PGN value");
		}
	}
}
