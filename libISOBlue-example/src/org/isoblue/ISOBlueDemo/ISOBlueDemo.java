/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.isoblue.ISOBlueDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.authorwjf.AboutDialog;

/**
 * This is the main Activity that displays the current chat session.
 */
public class ISOBlueDemo extends Activity {
    // Debugging
    private static final String TAG = "ISOBlueDemo";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ_ENG = 2;
    public static final int MESSAGE_READ_IMP = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Message argument 1 values
    public static final int MESSAGE_ARG1_NEW = 1;
    public static final int MESSAGE_ARG1_BUF = 2;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private ListView mEngList;
    private ListView mImpList;
    private ListView mBufEngList;
    private ListView mBufImpList;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mEngArrayAdapter;
    private ArrayAdapter<String> mImpArrayAdapter;
    private ArrayAdapter<String> mBufEngArrayAdapter;
    private ArrayAdapter<String> mBufImpArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;

    FragmentManager fm;
    protected static PGNDialogFragment PGNDialog;

    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase mDatabase;

    private boolean mPast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        fm = getFragmentManager();
        PGNDialog = new PGNDialogFragment();

        // Set up the window layout
        setContentView(R.layout.main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mHelper = new ISOBUSOpenHelper(this.getApplicationContext());
        mDatabase = mHelper.getWritableDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null)
                setupChat();
        }

        mDatabase.beginTransaction();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mEngArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mImpArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mEngList = (ListView) findViewById(R.id.eng);
        mEngList.setAdapter(mEngArrayAdapter);
        mImpList = (ListView) findViewById(R.id.imp);
        mImpList.setAdapter(mImpArrayAdapter);

        mBufEngArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mBufImpArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mBufEngList = (ListView) findViewById(R.id.buf_eng);
        mBufEngList.setAdapter(mBufEngArrayAdapter);
        mBufImpList = (ListView) findViewById(R.id.buf_imp);
        mBufImpList.setAdapter(mBufImpArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public void onStop() {
        super.onStop();

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
            mChatService.stop();

        mHelper.close();
    }

    /**
     * Sends a message.
     * 
     * @param message
     *            A string of text to send.
     */
    private void sendMessage(org.isoblue.isobus.Message message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Get the message bytes and tell the BluetoothChatService to write
        mChatService.write(message);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            org.isoblue.isobus.Message m;
            ContentValues values;

            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to,
                            mConnectedDeviceName));
                    mEngArrayAdapter.clear();
                    break;
                case BluetoothService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_READ_ENG:
                m = (org.isoblue.isobus.Message) msg.obj;
                values = new ContentValues();
                values.put(ISOBUSOpenHelper.COLUMN_PGN, m.getPgn().asInt());
                values.put(ISOBUSOpenHelper.COLUMN_DATA, m.getData());
                values.put(ISOBUSOpenHelper.COLUMN_SRC, m.getSrcAddr());
                values.put(ISOBUSOpenHelper.COLUMN_DEST, m.getDestAddr());
                values.put(ISOBUSOpenHelper.COLUMN_BUS, "engine");
                values.put(ISOBUSOpenHelper.COLUMN_TIME, m.getTimeStamp());
                mDatabase.insert(ISOBUSOpenHelper.TABLE_MESSAGES, null, values);
                switch (msg.arg1) {
                case MESSAGE_ARG1_NEW:
                    mEngArrayAdapter.add(m.toString());
                    break;
                case MESSAGE_ARG1_BUF:
                    mBufEngArrayAdapter.add(m.toString());
                    break;
                }
                postMessage("Engine", m);
                break;
            case MESSAGE_READ_IMP:
                m = (org.isoblue.isobus.Message) msg.obj;
                values = new ContentValues();
                values.put(ISOBUSOpenHelper.COLUMN_PGN, m.getPgn().asInt());
                values.put(ISOBUSOpenHelper.COLUMN_DATA, m.getData());
                values.put(ISOBUSOpenHelper.COLUMN_SRC, m.getSrcAddr());
                values.put(ISOBUSOpenHelper.COLUMN_DEST, m.getDestAddr());
                values.put(ISOBUSOpenHelper.COLUMN_BUS, "implement");
                values.put(ISOBUSOpenHelper.COLUMN_TIME, m.getTimeStamp());
                mDatabase.insert(ISOBUSOpenHelper.TABLE_MESSAGES, null, values);
                switch (msg.arg1) {
                case MESSAGE_ARG1_NEW:
                    mImpArrayAdapter.add(m.toString());
                    break;
                case MESSAGE_ARG1_BUF:
                    mBufImpArrayAdapter.add(m.toString());
                    break;
                }
                postMessage("Implement", m);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(),
                        "Connected to " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(),
                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                        .show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                try {
                    connectDevice(data, mPast);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean past) throws IOException,
            InterruptedException {
        // Get the device MAC address
        String address = data.getExtras().getString(
                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, past);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;

        case R.id.get_past_data:
            mPast = !item.isChecked();
            item.setChecked(mPast);
            return true;

        case R.id.select_pgns:
            PGNDialog.show(fm, "pgn_dialog");
            return true;

        case R.id.about:
            AboutDialog about = new AboutDialog(this);
            about.setTitle(R.string.about_title);
            about.show();
            break;
        }
        return false;
    }

    private void postMessage(String bus, org.isoblue.isobus.Message m) {
        new PostTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bus,
                Short.toString(m.getDestAddr()),
                Short.toString(m.getSrcAddr()), Arrays.toString(m.getData()),
                Long.toString(m.getTimeStamp()));
    }

    // Acquired from Cyrus
    private void postData(String bus, String dest, String src, String data,
            String time) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(
                "https://docs.google.com/forms/d/1mszF-1Dvk18ajb6WhP22ctkDqizNI9-wMjylDznPKjs/formResponse");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("entry.2120486804", bus));
            nameValuePairs.add(new BasicNameValuePair("entry.363860839", dest));
            nameValuePairs.add(new BasicNameValuePair("entry.80712516", src));
            nameValuePairs
                    .add(new BasicNameValuePair("entry.1759126747", data));
            nameValuePairs.add(new BasicNameValuePair("entry.478698627", time));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class PostTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            postData(params[0], params[1], params[2], params[3], params[4]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // This is executed on ui thread
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // This is executed on ui thread
        }
    }
}
