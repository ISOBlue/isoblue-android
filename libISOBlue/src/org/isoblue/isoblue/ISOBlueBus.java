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

package org.isoblue.isoblue;

import java.util.Collection;
import java.util.Set;

import org.isoblue.isobus.Bus;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;
import org.isoblue.isobus.PGN;

public class ISOBlueBus extends Bus {

	private ConstantIndexVector<ISOBUSSocket> mSocks;

	public ISOBlueBus(ISOBlueDevice network, BusType type) {
		super(network, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isoblue.isobus.Bus#attach(org.isoblue.isobus.ISOBUSSocket)
	 */
	@Override
	protected void attach(ISOBUSSocket sock) throws InterruptedException {
		Set<PGN> pgns;
		ISOBlueCommand cmd;
		short bus;
		StringBuilder s = new StringBuilder();

		super.attach(sock);

		pgns = sock.getPgns();

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

		s.append(String.format("%5x", pgns.size()));
		for (PGN pgn : pgns) {
			s.append(String.format("%5x", pgn.asInt()));
		}

		cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.FILT, bus, (short) 0,
				s.toString().getBytes());

		((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
	}

	@Override
	protected Collection<ISOBUSSocket> initSocks() {
		return mSocks = new ConstantIndexVector<ISOBUSSocket>();
	}

	protected void handleCommand(ISOBlueCommand cmd) {
		switch (cmd.getOpCode()) {
		case MESG:
			// TODO: Support multiple sockets per bus?
			Message message;
			// int sock;
			short saddr;
			short daddr;
			PGN pgn;
			long timestamp;
			int len;
			byte data[];
			String cmdData;

			cmdData = new String(cmd.getData());

			pgn = new PGN(Integer.parseInt(cmdData.substring(0, 5), 16));
			daddr = Short.parseShort(cmdData.substring(5, 7), 16);
			len = Integer.parseInt(cmdData.substring(7, 11), 16);
			data = new byte[len];
			int curs = 11;
			for (int i = 0; i < len; i++) {
				data[i] = (byte) Integer.parseInt(cmdData.substring(curs, curs + 2), 16);
				curs += 2;
			}
			timestamp = Long.parseLong(cmdData.substring(curs, curs + 8), 16) * 1000000
					+ Integer.parseInt(cmdData.substring(curs + 8, curs + 13), 16);
			saddr = Short.parseShort(cmdData.substring(curs + 13, curs + 15), 16);
			//Log.d("IBBUS", "bus:" + getType() + " data:\"" + str + "\" timestamp:" + timestamp + " saddr:" + saddr + " daddr:" + daddr);

			message = new Message(daddr, saddr, pgn, data, timestamp);

			for (ISOBUSSocket socket : mSocks) {
				super.passMessageIn(socket, message);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void passMessageOut(Message message) throws InterruptedException {
		ISOBlueCommand cmd;
		short bus;
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
		s.append(String.format("%5x%2x%4x", message.getPgn().asInt(), message.getDestAddr(), data.length));
		for (byte b : message.getData()) {
			s.append(String.format("%02x", b));
		}

		cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.WRITE, (short) 0, bus,
				s.toString().getBytes());
		((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
	}
}
