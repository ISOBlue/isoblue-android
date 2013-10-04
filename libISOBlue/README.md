libISOBlue
==========

Overview
--------
*libISOBlue* is an Android library for use with an ISOBlue device.
The idea is to transparently tunnel ISOBUS over Bluetooth to Android,
making it appear as though the Android device is directly connected
to the ISOBUS network which the *ISOBlue* device is on.

Usage
-----

### Connecting to *ISOBlue* ###
An app connects the library to an *ISOBlue* using the `BluetoothDevice`
corresponding to that *ISOBlue*.
Information on acquiring the `BluetoothDevice` object can be found [here][bt].
Here is example code for attempting to connect:
```Java
try {
	// Creating the ISOBlueDevice initiates the connection with the ISOBlue
	ISOBlueDevice ibd = new ISOBlueDevice(bluetoothDevice);
} catch(IOException e) {
	// The BluetoothDevice could not be connected to, or it is not an ISOBlue
}
```
[bt]: http://developer.android.com/guide/topics/connectivity/bluetooth.html#FindingDevices "Discovering Bluetooth Devices"

### Controlling Which PGNs to Receive ###
The library allows specifying a set of specific PGNs to receive.
Here is example code which specifies 2 particular PGNs:
```Java
// It doesn't have to be a HashSet, just any Set
Set<PGN> pgns = new HashSet<PGN>();
// Leave the Set empty to receive all PGNs

// Add specific PGN(s) to the Set to only receive them
try {
	pgns.add(new PGN(integerValue1));
	pgns.add(new PGN(integerValue2));
} catch(InvalidPGNException e) {
	// The number integerValue was not a valid ISOBUS PGN
	// This Exception does not have to be caught
}
```

### Creating Sockets ###
The library creates datagram sockets,
each of which is on a particular bus (engine or implement).
Here is example code which creates one socket on each bus:
```Java
// Create a socket on the engine bus
ISOBUSSocket engSocket = new ISOBUSSocket(idb.getEngineBus(), null, pgns);

// Create a socket on the implement bus
ISOBUSSocket impSocket = new ISOBUSSocket(idb.getImplementBUS(), null, pgns);
```

### Receiving Messages from a Socket ###
Sockets created from the library can be used to receive messages.
Each call to `read()` returns a single `Message`,
which represents an ISOBUS messages which was on that socket's bus
and matched that socket's PGN set.
When messages are received they are queued until returned by `read()`,
and if there are no messages queued
`read()` will block until a message is received.
Here is example code which reads a single message from an `ISOBUSSocket`:
```Java
try {
	// This method blocks if no Messages are ready
	Message message = socket.read();
} catch(InterruptedException e) {
	// Thrown if the thread calling read can is interrupted for some reason
}
```

