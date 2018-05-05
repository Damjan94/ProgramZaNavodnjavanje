package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by damjan on 3/24/18.
 */

public class ConnectThread extends Thread {
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private final static String TAG = "Bluetooth connect thread";

	private final BluetoothSocket m_Socket;
	private final IBluetoothComms m_comms;
	private final int m_connectRetryCount;

	public ConnectThread(BluetoothDevice device, IBluetoothComms comms) throws IOException
	{
		this(device, comms, 3);
	}
	ConnectThread(BluetoothDevice device, IBluetoothComms comms, int connectRetryCount) throws IOException
	{
		m_comms = comms;

		UUID uuid = UUID.fromString(MY_UUID);
		m_Socket = device.createRfcommSocketToServiceRecord(uuid);

		m_connectRetryCount = connectRetryCount;
	}
	public void run()
	{
		// Cancel discovery because it otherwise slows down the connection.
		//mBluetoothAdapter.cancelDiscovery();
		int tryCount = 0;
		do
		{
			try
			{
				// Connect to the remote device through the socket. This call blocks
				// until it succeeds or throws an exception.
				m_Socket.connect();
				m_comms.connected(m_Socket);
			} catch (IOException connectException)
			{
				// Unable to connect
				Log.e(TAG, "Could not connect the socket", connectException);
				tryCount++;
			}
			if(tryCount > m_connectRetryCount)
			{
				break;
			}
		}while(!m_Socket.isConnected());
	}
}