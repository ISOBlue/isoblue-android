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
	public Set<PGN> getPgns() {
		return mPgns;
	}
}
