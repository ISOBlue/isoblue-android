package org.isoblue.isoblue;

import java.util.Collection;
import java.util.Scanner;

import org.isoblue.isobus.Bus;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;
import org.isoblue.isobus.PGN;

import android.util.Log;

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
			str += " " + pgn.asInt();
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
			Scanner s;
			Message message;
			// int sock;
			short saddr;
			short daddr;
			PGN pgn;
			long timestamp;
			int len;
			byte data[];

			Log.d("IBBUS", "bus:" + getType());

			String str;
			str = new String(cmd.getData());
			Log.d("IBBUS", "data:\"" + str + "\"");
			s = new Scanner(str);

			// sock = s.nextInt();
			pgn = new PGN(s.nextInt());
			len = s.nextInt();
			data = new byte[len];
			for (int i = 0; i < len; i++) {
				data[i] = (byte) s.nextInt(16);
			}
			timestamp = (long) (s.nextDouble() * 1000000);
			Log.d("IBBUS", "timestamp:" + timestamp);
			saddr = s.nextShort(16);
			Log.d("IBBUS", "saddr:" + saddr);
			daddr = s.nextShort(16);

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

		str = message.getDestAddr() + " " + message.getPgn().asInt() + " "
				+ message.getData().length;
		for (byte b : message.getData()) {
			str += " " + String.format("%02x", b);
		}

		cmd = new ISOBlueCommand(ISOBlueCommand.OpCode.WRITE, (short) 0, bus,
				str.getBytes());
		((ISOBlueDevice) super.getNetwork()).sendCommand(cmd);
	}
}
