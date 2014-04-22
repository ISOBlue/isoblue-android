/*
 * Author: Alex Layton <awlayton@purdue.edu>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.isoblue.isobus.Bus;
import org.isoblue.isobus.ISOBUSNetwork;
import org.isoblue.isobus.ISOBUSSocket;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ISOBlueDevice extends ISOBUSNetwork {

    private static final UUID MY_UUID = UUID
            .fromString("00000000-0000-0000-0000-00000000abcd");
    private static final byte[] MY_PIN = { '0', '0', '0', '0' };

    private BluetoothDevice mDevice;
    private volatile BluetoothSocket mSocket;
    private ISOBlueBus mEngineBus, mImplementBus;
    private Thread mReadThread, mWriteThread;
    private BlockingQueue<ISOBlueCommand> mOutCommands;

    private transient Serializable mStartId;
    private transient Object mStartIdLock;

    public ISOBlueDevice(BluetoothDevice device) throws IOException {
        mDevice = device;

        mEngineBus = new ISOBlueBus(this, ISOBlueBus.BusType.ENGINE);
        mImplementBus = new ISOBlueBus(this, ISOBlueBus.BusType.IMPLEMENT);

        mOutCommands = new LinkedBlockingQueue<ISOBlueCommand>();

        try {
            device.getClass().getMethod("setPin", byte[].class)
                    .invoke(device, MY_PIN);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        mSocket.connect();

        mReadThread = new ReadThread();
        mWriteThread = new WriteThread();

        mStartId = null;
        mStartIdLock = new Object();

        mReadThread.start();
        mWriteThread.start();
    }

    /**
     * Create a pair of {@link BufferedISOBUSSocket}s which will receive all
     * {@link Message}s stored by ISOBlue coming after the specified one. <br>
     * One socket will receive engine bus messages and the other will receive
     * implement bus messages.
     * 
     * @param fromId
     *            the ID corresponding to the {@link Message} after which these
     *            sockets will start receiving
     * @return An array containing two buffered sockets. <br>
     *         Index 0 contains the socket which will receive engine bus
     *         messages. <br>
     *         Index 1 contains the socket which will receive implement bus
     *         messages.
     * @throws IOException
     * @throws InterruptedException
     */
    public ISOBUSSocket[] createBufferedISOBUSSockets(Serializable fromId)
            throws IOException, InterruptedException {
        BufferedISOBUSSocket[] socks = new BufferedISOBUSSocket[2];
        Serializable toId;

        toId = getStartId();

        // Create socket for past engine messages
        socks[0] = new BufferedISOBUSSocket(fromId, toId, mEngineBus, null,
                null);
        // Create socket for past implement messages
        socks[1] = new BufferedISOBUSSocket(fromId, toId, mImplementBus, null,
                null);

        // Create command to ask ISOBlue for past data
        sendCommand((new ISOBlueCommand(ISOBlueCommand.OpCode.PAST, (byte) -1,
                (byte) -1, String.format("%08x%08x", fromId, toId).getBytes())));

        return socks;
    }

    private synchronized BluetoothSocket reconnectSocket() {
        try {
            mSocket.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mSocket = null;
        while (mSocket == null) {
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                mSocket.connect();
            } catch (IOException e) {
                mSocket = null;

                // TODO Auto-generated catch block
                e.printStackTrace();
                android.os.SystemClock.sleep(100);
            }
        }

        return mSocket;
    }

    protected void sendCommand(ISOBlueCommand cmd) throws InterruptedException {
        mOutCommands.put(cmd);

        Log.d("CMD", cmd.toString());
    }

    public Bus getEngineBus() {
        return mEngineBus;
    }

    public Bus getImplementBus() {
        return mImplementBus;
    }

    protected Serializable getStartId() {
        synchronized (mStartIdLock) {
            while (mStartId == null) {
                try {
                    mStartIdLock.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return mStartId;
    }

    private class ReadThread extends Thread {

        private BufferedReader mReader;

        private ReadThread() throws IOException {
            mReader = new BufferedReader(new InputStreamReader(
                    mSocket.getInputStream()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            while (true) {
                while (true) {
                    String line;

                    // Receive the command
                    try {
                        line = mReader.readLine();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    }
                    Log.d("CMD", line);

                    // Parse the command
                    try {
                        ISOBlueCommand cmd;
                        Serializable id;

                        cmd = ISOBlueCommand.receiveCommand(line);

                        switch (cmd.getBus()) {
                        case 0:
                            id = mEngineBus.handleCommand(cmd);
                            break;

                        case 1:
                            id = mImplementBus.handleCommand(cmd);
                            break;

                        default:
                            continue;
                        }

                        // TODO: Do this one time assignment more efficiently?
                        if (mStartId == null
                                && cmd.getOpCode().equals(
                                        ISOBlueCommand.OpCode.MESG)) {
                            synchronized (mStartIdLock) {
                                mStartId = id;
                                mStartIdLock.notifyAll();
                            }
                        }
                    } catch (RuntimeException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        continue;
                    }
                }

                synchronized (mSocket) {
                    try {
                        reconnectSocket();
                        mReader = new BufferedReader(new InputStreamReader(
                                mSocket.getInputStream()));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class WriteThread extends Thread {

        private OutputStream mOut;

        private WriteThread() throws IOException {
            mOut = mSocket.getOutputStream();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            ISOBlueCommand cmd;

            while (true) {
                while (true) {
                    try {
                        cmd = mOutCommands.take();

                        cmd.sendCommand(mOut);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    } catch (NullPointerException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    }
                }

                synchronized (mSocket) {
                    try {
                        mOut = mSocket.getOutputStream();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @return the mDevice
     */
    public BluetoothDevice getDevice() {
        return mDevice;
    }
}
