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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents a Socket style connection to a {@link Bus} on an ISOBUS Network.
 * <p>
 * This class is used to send and receive {@link Message}s using
 * {@link #write(Message)} and {@link #read()} respectively.
 * 
 * @see ISOBUSNetwork
 * @see Bus
 * @see Message
 * @author Alex Layton <alex@layton.in>
 */
public class ISOBUSSocket implements Closeable {

    /**
     * ISOBUS Bus to which this {@link ISOBUSSocket} is connected.
     */
    private Bus mBus;

    /**
     * ISOBUS NAME used to identify the user of this {@link ISOBUSSocket} on the
     * network.
     */
    // TODO: Implement using this
    private NAME mName;

    /**
     * Used to filter what is received on this {@link ISOBUSSocket}.
     * <p>
     * If it is empty, no filtering is done.
     * 
     * @see Message
     */
    private Set<PGN> mPgns;

    /**
     * Buffer for what has been received but not yet read.
     */
    private BlockingQueue<Message> mInMessages;

    /**
     * Construct a new {@link ISOBUSSocket} connected to the given {@link Bus},
     * using the given {@link NAME}, and receiving {@link Message}s with with
     * one of the given {@link PGN}s.
     * <p>
     * If no {@link PGN}s are given, all {@link Message}s are received.
     * 
     * @param bus
     *            {@link Bus} to which to create a connection
     * @param name
     *            not yet used
     * @param pgns
     *            {@link PGN}s to receive, null treated as empty
     * @throws IOException
     *             when connecting to {@code bus} fails
     */
    public ISOBUSSocket(Bus bus, NAME name, Collection<PGN> pgns)
            throws IOException {
        mBus = bus;
        mName = name;

        mPgns = pgns == null ? new HashSet<PGN>() : new HashSet<PGN>(pgns);

        mInMessages = new LinkedBlockingQueue<Message>();

        if (!mBus.attach(this)) {
            throw new IOException("Could not connect to bus: " + bus);
        }
    }

    /**
     * Writes (sends) the given {@link Message} using this {@link ISOBUSSocket}.
     * This method may block if there is not room in corresponding the out
     * buffer.
     * 
     * @param message
     *            {@link Message} to write
     * @throws InterruptedException
     *             if interrupted while waiting for buffer space
     */
    public void write(Message message) throws InterruptedException {
        mBus.passMessageOut(message);
    }

    /**
     * Reads (receives) a {@link Message} that came to this {@link ISOBUSSOcket}
     * . This method blocks if the in buffer is empty.
     * 
     * @return a received {link Message}
     * @throws InterruptedException
     *             if interrupted while waiting for a {@link Message}
     * 
     * @see #ISOBUSSocket(Bus, NAME, Collection)
     */
    public Message read() throws InterruptedException {
        return mInMessages.take();
    }

    protected boolean receive(Message message) {
        return mInMessages.offer(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        mBus.detach(this);
    }

    /**
     * Get the {@link Bus} to which this {@link ISOBUSSocket} is connected.
     * 
     * @return the {@link Bus}
     */
    public Bus getBus() {
        return mBus;
    }

    /**
     * Get the {@link NAME} used by this {@link ISOBUSSocket}.
     * 
     * @return the {@link NAME}
     */
    public NAME getName() {
        return mName;
    }

    /**
     * Get the {@link Set} of {@link PGN}s which this {@link ISOBUSSocket}'s
     * received {@link Message}s can have.
     * 
     * @return the {@link Set} of {@link PGN}s
     */
    public Set<PGN> getPgns() {
        return mPgns;
    }
}
