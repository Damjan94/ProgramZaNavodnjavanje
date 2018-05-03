package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by damjan on 3/24/18.
 */

class ConnectThread extends Thread {
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private final static String TAG = "Bluetooth connect thread";
	private final BluetoothSocket m_Socket;
	private final BluetoothComms m_comms;
	ConnectThread(BluetoothDevice device, BluetoothComms comms) {
		m_comms = comms;
		// Use a temporary object that is later assigned to m_Socket
		// because m_Socket is final.
		BluetoothSocket tmp = null;
		String name = device.getName();
		try {
			// Get a BluetoothSocket to connect with the given BluetoothDevice.
			// MY_UUID is the app's UUID string, also used in the server code.
			UUID uuid = UUID.fromString(MY_UUID);
			tmp = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			Log.e(TAG, "Socket's create() method failed", e);
		}
		m_Socket = tmp;
	}
	
	public void run() {
		// Cancel discovery because it otherwise slows down the connection.
		//mBluetoothAdapter.cancelDiscovery();
		do
		{
			try
			{
				// Connect to the remote device through the socket. This call blocks
				// until it succeeds or throws an exception.
				m_Socket.connect();
				
				// The connection attempt succeeded. Perform work associated with
				// the connection in a separate thread.
				
			} catch (IOException connectException)
			{
				// Unable to connect; close the socket and return.
				Log.e(TAG, "Could not connect the socket", connectException);
				/*
				try
				{
					m_Socket.close();
				} catch (IOException closeException)
				{
					Log.e(TAG, "Could not close the client socket", closeException);
				}*/
			}
		}while(!m_Socket.isConnected());
		m_comms.setSocket(m_Socket);
		m_comms.run();
		
	}
	
	// Closes the client socket and causes the thread to finish.
	public void cancel() {
		try {
			m_Socket.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not close the client socket", e);
		}
	}
}