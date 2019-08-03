package com.example.damjan.programzanavodnjavanje.data.bluetooth;

import com.example.damjan.programzanavodnjavanje.data.Error;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.util.Calendar;

/**
 * Created by damjan on 4/5/18.
 */

public interface IBluetoothComms
{
    void connected();

    void connectionFailed();

    void disconnected();

    void setTemperature(String temperature);

    void setTime(Calendar time);

    void setValves(ValveOptionsData[] valves);

    void setErrors(Error[] errors);
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
		((ISetValveData)(MainActivity.mainActivity)).enableSendButton();
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