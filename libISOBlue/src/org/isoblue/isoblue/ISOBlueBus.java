/*
 * Author: Alex Layton <awlayton@purdue.edu>
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
		Collection<PGN> pgns;
		ISOBlueCommand cmd;
		short bus;
		String str;

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

		str = Integer.toString(pgns.size());
		for (PGN pgn : pgns) {
			str += " " + pgn.getValue();
		}

		cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.FILT, bus, (short) 0,
				str.getBytes());

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
			String tokens[];
			// int sock;
			short saddr;
			short daddr;
			PGN pgn;
			long timestamp;
			int len;
			byte data[];


			tokens = (new String(cmd.getData())).split(" ");

			// sock = s.nextInt();
			pgn = new PGN(Integer.parseInt(tokens[0]));
			len = Integer.parseInt(tokens[1]);
			data = new byte[len];
			for (int i = 0; i < len; i++) {
				data[i] = (byte) Integer.parseInt(tokens[2+i], 16);
			}
			timestamp = (long) (Double.parseDouble(tokens[len+2]) * 1000000);
			saddr = Short.parseShort(tokens[len+3], 16);
			daddr = Short.parseShort(tokens[len+4], 16);
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
		String str;

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

		str = message.getDestAddr() + " " + message.getPgn().getValue() + " "
				+ message.getData().length;
		for (byte b : message.getData()) {
			str += " " + String.format("%02x", b);
		}

		cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.WRITE, (short) 0, bus,
				str.getBytes());
		((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
	}
}
