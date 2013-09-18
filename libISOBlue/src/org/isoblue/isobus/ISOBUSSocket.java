package org.isoblue.isobus;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ISOBUSSocket implements Closeable {

	private Bus mBus;
	private NAME mName;
	private Set<PGN> mPgns;
	private BlockingQueue<Message> mInMessages;

	public ISOBUSSocket(Bus bus, NAME name, Collection<PGN> pgns)
			throws InterruptedException {
		mBus = bus;
		mName = name;

		mPgns = new HashSet<PGN>(pgns);

		mInMessages = new LinkedBlockingQueue<Message>();

		mBus.attach(this);
	}

	public void write(Message message) throws InterruptedException {
		mBus.passMessageOut(message);
	}

	public Message read() throws InterruptedException {
		return mInMessages.take();
	}

	protected boolean receive(Message message) {
		return mInMessages.offer(message);
	}

	public void close() {
		mBus.detach(this);
	}

	/**
	 * @return the mBus
	 */
	public Bus getBus() {
		return mBus;
	}

	/**
	 * @return the mName
	 */
	public NAME getName() {
		return mName;
	}

	/**
	 * @return the mPgns
	 */
	public Collection<PGN> getPgns() {
		return mPgns;
	}
}
