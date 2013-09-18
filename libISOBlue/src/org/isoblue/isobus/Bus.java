package org.isoblue.isobus;

import java.util.Collection;

public abstract class Bus {

	public enum BusType {
		ENGINE, IMPLEMENT,
	}

	private ISOBUSNetwork mNetwork;
	private BusType mType;
	private Collection<ISOBUSSocket> mSocks;

	public Bus(ISOBUSNetwork network, BusType type) {
		mNetwork = network;
		mType = type;

		mSocks = initSocks();
	}

	protected abstract Collection<ISOBUSSocket> initSocks();

	protected void passMessageIn(ISOBUSSocket socket, Message message) {
		socket.receive(message);
	}

	protected abstract void passMessageOut(Message message)
			throws InterruptedException;

	protected void attach(ISOBUSSocket sock) throws InterruptedException {
		mSocks.add(sock);
	}

	protected void detach(ISOBUSSocket sock) {
		mSocks.remove(sock);
	}

	public ISOBUSNetwork getNetwork() {
		return mNetwork;
	}

	public BusType getType() {
		return mType;
	}
}
