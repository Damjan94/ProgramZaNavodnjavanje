package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.IComm;
import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by damjan on 4/5/18.
 */

public interface IBluetoothComms
{

	void connected(BluetoothSocket socket);

	void setTemperature(float aFloat);

	void setTime(Calendar timeAsync);

	void setValves(ValveOptionsData[] valvesAsync);
}























	/*
	private InputStream m_input;
	private OutputStream m_output;
	private final ArrayBlockingQueue<Byte> bytes;
	private final int MAX_BUFFER_SIZE = 2048;
	public BluetoothComms()
	{
		bytes = new ArrayBlockingQueue<>(MAX_BUFFER_SIZE);
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!btAdapter.isEnabled())
		{
			btAdapter.enable();//TODO temp code, remove when not needed
			try
			{
				Thread.sleep(200);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		BluetoothDevice device = btAdapter.getBondedDevices().iterator().next();
		ConnectThread connectThread = new ConnectThread(device, this);
		connectThread.start();
	}
	
	@Override
	public void run()
	{
		((IComm)(MainActivity.mainActivity)).enableSendButton();
		while(true)
		{
			byte[] buffer = new byte[MAX_BUFFER_SIZE];
			int readSize;
			try
			{
				readSize = m_input.read(buffer);
				for (int i = 0; i < readSize; i++)
				{
					bytes.put(buffer[i]);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	public boolean write(int code)
	{
		if(m_output == null)
		{
			return false;
		}
		try
		{
			m_output.write(code);
			m_output.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	public boolean write(byte code)
	{
		if(m_output == null)
		{
			return false;
		}
		try
		{
			m_output.write(code);
			m_output.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	public byte read() throws InterruptedException
	{
		return bytes.take();
	}
	
	void connected(BluetoothSocket socket)
	{
		if(!socket.isConnected())
		{
			return;
		}
		try
		{
			m_input = socket.getInputStream();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			m_output = socket.getOutputStream();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		MainActivity.mainActivity.runOnUiThread(()->{Toast.makeText(MainActivity.mainActivity, "Connected to:"+socket.getRemoteDevice().getName(), Toast.LENGTH_SHORT).show();});
	}
}
*/