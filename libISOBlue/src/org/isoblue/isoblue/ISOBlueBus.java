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

package org.isoblue.isoblue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.isoblue.isobus.Bus;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;
import org.isoblue.isobus.PGN;

public class ISOBlueBus extends Bus {

    private ConstantIndexVector<ISOBUSSocket> mSocks;
    private ConstantIndexVector<BufferedISOBUSSocket> mBufferedSocks;

    protected static final Message MESSAGE_NONE = new Message((short) 0, new PGN(0),
            new byte[0]) {

        private static final long serialVersionUID = -4975954908492457312L;

        /*
         * (non-Javadoc)
         *
         * @see org.isoblue.isobus.Message#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            return this == o;
        }
    };

    public ISOBlueBus(ISOBlueDevice network, BusType type) {
        super(network, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.isoblue.isobus.Bus#attach(org.isoblue.isobus.ISOBUSSocket)
     */
    @Override
    protected boolean attach(ISOBUSSocket sock) {
        Set<PGN> pgns;
        ISOBlueCommand cmd;
        StringBuilder s = new StringBuilder();

        if (!super.attach(sock)) {
            return false;
        }
        pgns = sock.getPgns();

        s.append(String.format("%05x", pgns.size()));
        for (PGN pgn : pgns) {
            s.append(String.format("%05x", pgn.asInt()));
        }

        cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.FILT, getType(),
                (byte) 0, s.toString().getBytes());

        try {
            ((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean attach(BufferedISOBUSSocket sock) {
        mBufferedSocks.add(sock);
        return true;
    }

    @Override
    protected Collection<ISOBUSSocket> initSocks() {
        mBufferedSocks = new ConstantIndexVector<BufferedISOBUSSocket>();
        return mSocks = new ConstantIndexVector<ISOBUSSocket>();
    }

    protected Serializable handleCommand(ISOBlueCommand cmd) {
        Collection<? extends ISOBUSSocket> sockets;

        switch (cmd.getOpCode()) {
        case MESG:
            sockets = mSocks;
            break;

        case OLD_MESG:
            sockets = mBufferedSocks;
            break;

        default:
            return null;
        }

        // TODO: Support multiple sockets per bus?
        // int sock;
        String cmdData;
        Serializable id;
        Message message;

        cmdData = new String(cmd.getData());

        id = Integer.parseInt(cmdData.substring(0, 8), 16);
        if (id.equals(0)) {
            message = MESSAGE_NONE;
        } else {
            short saddr;
            short daddr;
            PGN pgn;
            long timestamp;
            int len;
            byte data[];

            pgn = new PGN(Integer.parseInt(cmdData.substring(8, 13), 16));
            daddr = Short.parseShort(cmdData.substring(13, 15), 16);
            len = Integer.parseInt(cmdData.substring(15, 19), 16);
            data = new byte[len];
            int curs = 19;
            for (int i = 0; i < len; i++) {
                data[i] = (byte) Integer.parseInt(
                        cmdData.substring(curs, curs + 2), 16);
                curs += 2;
            }
            timestamp = Long.parseLong(cmdData.substring(curs, curs + 8), 16)
                    * 1000000
                    + Integer.parseInt(cmdData.substring(curs + 8, curs + 13),
                            16);
            saddr = Short.parseShort(cmdData.substring(curs + 13, curs + 15),
                    16);
            // Log.d("IBBUS", "bus:" + getType() + " data:\"" + str +
            // "\" timestamp:" + timestamp + " saddr:" + saddr + " daddr:" +
            // daddr);

            message = new Message(id, daddr, saddr, pgn, data, timestamp);
        }

        for (ISOBUSSocket socket : sockets) {
            super.passMessageIn(socket, message);
        }

        return id;
    }

    @Override
    protected void passMessageOut(Message message) throws InterruptedException {
        ISOBlueCommand cmd;
        byte bus;
        byte data[];
        StringBuilder s = new StringBuilder();

        switch (super.getType()) {
        case ENGINE:
            bus = 0;
            break;

        case IMPLEMENT:
            bus = 1;
            break;

        default:
            bus = -1;
            break;
        }

        data = message.getData();
        s.append(String.format("%5x%2x%4x", message.getPgn().asInt(),
                message.getDestAddr(), data.length));
        for (byte b : message.getData()) {
            s.append(String.format("%02x", b));
        }

        cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.WRITE, (byte) 0, bus, s
                .toString().getBytes());
        ((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
    }
}
