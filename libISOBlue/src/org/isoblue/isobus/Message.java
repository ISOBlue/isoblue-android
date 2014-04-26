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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents an ISOBUS message. Messages are the datagrams which are
 * sent/received on the ISOBUS network, using {@link ISOBUSSocket#read()} and
 * {@link ISOBUSSocket#write(Message)} respectively.
 *
 * @see ISOBUSSocket
 * @author Alex Layton <alex@layton.in>
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 9109136928782406932L;

    /**
     * Identifier of this {@link Message}.
     */
    private final Serializable mId;

    // TODO: Use NAMEs instead of addresses for source/destination
    /**
     * ISOBUS NAME of the destination of this {@link Message}.
     * 
     * @see NAME
     */
    // private final NAME mDestName;
    /**
     * ISOBUS NAME of the destination of this {@link Message}.
     * 
     * @see NAME
     */
    // private final NAME mSrcName;

    /**
     * ISOBUS address of the destination of this {@link Message}.
     */
    private final short mDestAddr;

    /**
     * ISOBUS address of the source of this {@link Message}.
     */
    private final short mSrcAddr;

    /**
     * {@link PGN} corresponding to the data of this {@link Message}. It is
     * needed to know how to interpret the {@code bytes} in {@link #mData}.
     */
    private final PGN mPgn;

    /**
     * Bytes of the contained data. The meaning of these bytes is determined by
     * {@link #mPgn}.
     */
    private final byte mData[];

    /**
     * Time when this {@link Message} was received, in &micros since the epoch.
     */
    private final long mTimestamp;

    /**
     * Stores the hash code of this {@link Message} to avoid recalculating it.
     * Initialized lazily since {@link Message} is immutable.
     */
    private transient int fHashCode;

    /**
     * Constructs a new {@link Message} with the specified destination,
     * {@link PGN}, and data.
     * 
     * @param destAddr
     *            the address of the destination
     * @param pgn
     *            the {@link PGN} corresponding to {@code data}, not null
     * @param data
     *            {@code bytes} of data, {@code null} treated as empty
     *            {@code byte[]}
     * 
     * @see PGN
     */
    public Message(short destAddr, PGN pgn, byte data[]) {
        this(null, destAddr, (short) -1, pgn, data, -1);
    }

    /**
     * Constructs a new {@link Message} with the specified destination, source,
     * {@link PGN}, data, and timestamp.
     * 
     * @param id
     *            the identifier assigned to this message
     * @param destAddr
     *            the address of the destination
     * @param srcAddr
     *            the address of the source
     * @param pgn
     *            the {@link PGN} corresponding to {@code data}, not null
     * @param data
     *            {@code bytes} of data, {@code null} treated as empty
     *            {@code byte[]}
     * @param timeStamp
     *            the arrival time of the {@link Message}, in &micros since the
     *            epoch
     * 
     * @see PGN
     */
    public Message(Serializable id, short destAddr, short srcAddr, PGN pgn,
            byte data[], long timeStamp) {
        if (pgn == null) {
            throw new NullPointerException("Parameter pgn was null");
        }

        mId = id;
        mDestAddr = destAddr;
        mSrcAddr = srcAddr;
        mPgn = pgn;
        // Handle data being null, and copy it so it won't change on us
        mData = data == null ? new byte[0] : data.clone();
        mTimestamp = timeStamp;
    }

    /**
     * Get the identifier assigned to this {@link Message}
     *
     * @return the id
     */
    public Serializable getId() {
        return mId;
    }

    /**
     * Get the source address of this {@link Message}.
     * 
     * @return the source address
     */
    public short getSrcAddr() {
        return mSrcAddr;
    }

    /**
     * Get the destination address of this {@link Message}.
     * 
     * @return the source address
     */
    public short getDestAddr() {
        return mDestAddr;
    }

    /**
     * Get the {@link PGN} corresponding to this {@link Message}'s data.
     * 
     * @return the {@link PGN}
     */
    public PGN getPgn() {
        return mPgn;
    }

    /**
     * Get the data {@code bytes} of this {@link Message}.
     * 
     * @return the data {@code bytes}
     */
    public byte[] getData() {
        return mData;
    }

    /**
     * Get the timestamp of this {@link Message}, in &micros since the epoch
     * 
     * @return the timestamps
     */
    public long getTimeStamp() {
        return mTimestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        if (mId != null) {
            s.append("ID:").append(mId).append(" ");
        }
        s.append("PGN:").append(mPgn.asInt());
        s.append(" SA:").append(mSrcAddr);
        s.append(" DA:").append(mDestAddr);

        s.append(" Data:");
        for (byte b : mData) {
            int val;

            // Put byte's value into and int, treating byte as unsigned
            val = ((int) b) & 0xff;

            if (val < 0x10)
                s.append("0");
            s.append(Integer.toString(val, 16)).append(" ");
        }

        s.append("Time: ").append(mTimestamp);

        return s.toString();
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code Message} with the same values for its destination, source, PGN,
     * data, timestamp, and id.
     * 
     * @param o
     *            the object to compare this {@link PGN} with
     * @return {@code true} if the specified object is equal to this
     *         {@code Integer}; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Message)) {
            return false;
        }

        Message m = (Message) o;

        return m.mDestAddr == this.mDestAddr && m.mSrcAddr == this.mSrcAddr
                && m.mPgn.equals(this.mPgn) && m.mData.equals(this.mData)
                && m.mTimestamp == this.mTimestamp && m.mId.equals(this.mId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (fHashCode == 0) {
            int result = 7;

            if (mId != null) {
                result = 31 * result + mId.hashCode();
            }
            result = 31 * result + mDestAddr;
            result = 31 * result + mSrcAddr;
            result = 31 * result + mPgn.hashCode();
            result = 31 * result + Arrays.hashCode(mData);
            result = 31 * result + (int) (mTimestamp ^ (mTimestamp >>> 32));

            fHashCode = result;
        }

        return fHashCode;
    }
}
