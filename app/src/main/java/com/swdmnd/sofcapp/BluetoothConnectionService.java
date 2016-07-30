package com.swdmnd.sofcapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Arief on 9/2/2015.
 * A service for handling bluetooth connection
 */
public class BluetoothConnectionService {
    BluetoothAdapter mBluetoothAdapter = null;
    private final Handler mHandler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket mBluetoothSocket = null;
    boolean connected = false;
    ConnectThread mConnectThread = null;
    ConnectedThread mConnectedThread = null;
    byte buffer[] = new byte[1024];
    String dataString = "";
    int counter =0;
    Activity parentActivity;

    private final static String LOG_TAG = "Bluetooth Service";

    public BluetoothConnectionService(Context context, Handler handler, BluetoothAdapter bluetoothAdapter){
        mBluetoothAdapter = bluetoothAdapter;
        mHandler = handler;
        parentActivity = (Activity) context;
    }

    public synchronized void start(){
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        else if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public void connect(BluetoothDevice device){
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        else if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public void write(byte[] bytes){
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mConnectedThread==null) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(bytes);
    }

    public void cancel(){
        if (mConnectedThread!=null){
                mConnectedThread.cancel();
        }

        if (mBluetoothSocket!=null) {
            try {
                mBluetoothSocket.close();
                connected = false;
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
    }

    private class ConnectThread extends Thread {
        BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device){
            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.
            mmDevice = device;
            try {
                mBluetoothSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }

        public void run(){
            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            mBluetoothAdapter.cancelDiscovery();
            // Establish the connection.  This will block until it connects.
            try {
                mBluetoothSocket.connect();
                connected = true;
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(LOG_TAG, e.toString());
                }
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionService.this) {
                mConnectThread = null;
            }

            mConnectedThread = new ConnectedThread(mBluetoothSocket);
            mConnectedThread.start();
        }

        public void cancel(){
            try {
                mBluetoothSocket.close();
                connected = false;
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
    }

    private class ConnectedThread extends Thread {
        BluetoothSocket mmBluetoothSocket;
        OutputStream outputStream;
        InputStream inputStream;

        ConnectedThread(BluetoothSocket socket){
            mmBluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run(){
            while (true) {
                try {
                    // Read from the InputStream
                    //Read is synchronous call which keeps on waiting until data is available
                    int bytes = inputStream.read(buffer, counter++, 1);
                    if (bytes > 0) {
                        if((char)buffer[counter-1] == '\n' || counter > 1022){
                            dataString = "";
                            dataString = new String(buffer, 0, counter-1);
                            mHandler.obtainMessage(Constants.MESSAGE_READ, dataString).sendToTarget();
                            buffer = new byte[1024];
                            counter = 0;
                        }
                    } else if (bytes == -1){
                        if(counter > 0){
                            dataString = "";
                            dataString = new String(buffer, 0, counter);
                            mHandler.obtainMessage(Constants.MESSAGE_READ, dataString).sendToTarget();
                            buffer = new byte[1024];
                            counter = 0;
                        }
                    }
                } catch (IOException e) {
                    Log.i(LOG_TAG, e.toString());
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);
            } catch (IOException e){
                Log.e(LOG_TAG, e.toString());
            }
        }
        public void cancel(){
            try{
                if(mmBluetoothSocket != null) mmBluetoothSocket.close();
                if(inputStream != null) inputStream.close();
                if(outputStream != null) outputStream.close();
                connected = false;
            } catch (IOException e){
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
}
