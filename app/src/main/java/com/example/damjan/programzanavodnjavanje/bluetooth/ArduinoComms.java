package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ArduinoComms extends Thread
{

	private static final ArduinoComms ONLY_INSTANCE = new ArduinoComms();

	static
	{
		ONLY_INSTANCE.start();
	}

	private ArduinoComms()
	{
	}

	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	private final static byte SEND_VALVE = 0x2c;
	private final static byte RECEIVE_VALVE = 0x1c;

	private final static byte RECEIVE_TEMP = 0x2b;
	private final static byte RECEIVE_TEMP_FLOAT = 0x1b;

	private final static byte SEND_TIME = 0x2a;
	private final static byte RECEIVE_TIME = 0x1a;

	private static final BlockingQueue<Runnable> TASK_LIST = new LinkedBlockingQueue<>();

	private static InputStream inputStream;
	private static OutputStream outputStream;

	private static ArrayList<IBluetoothComms> comms = new ArrayList<>();
	private static BluetoothSocket socket;

	@Override
	public void run()
	{
		while(true)
		{
			try {
				TASK_LIST.take().run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void connect(final BluetoothDevice device)
	{
		connect(device, 3);
	}
	public static void connect(final BluetoothDevice device, int connectRetryCount)
	{

		TASK_LIST.add(()->
		{
			if(socket != null && socket.isConnected() && socket.getRemoteDevice().equals(device))
			{
				return;
			}

			disconnectInternal();

			UUID uuid = UUID.fromString(MY_UUID);
			try {
				socket = device.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
				notifyConnectionFailed();
				return;
			}
			int retryCount = 0;
			do {
				try {
					socket.connect();
					inputStream = socket.getInputStream();
					outputStream = socket.getOutputStream();
				} catch (IOException e) {
					ConsoleActivity.log(e.toString()+'\n');
					retryCount++;
				}
			}while(retryCount < connectRetryCount && !socket.isConnected());
			if(socket.isConnected())
			{
				notifyConnected();
			}
			else
			{
				//failed to connect...
				notifyConnectionFailed();
			}
		});
	}

	//we add the task to notify disconnected because
	//disconnectInternal is used internally and we don't want to
	//notify listeners from internal calls
	public static void disconnect()
	{
		TASK_LIST.add(ArduinoComms::disconnectInternal);
		TASK_LIST.add(ArduinoComms::notifyDisconnected);
		//TODO if waiting at socket.connect from connect() notify the tread
	}

	private static void disconnectInternal()
	{
		try {
			if(socket == null)
			{
				return;
			}
			socket.close();
			socket = null;
			inputStream = null;
			outputStream = null;
		} catch (IOException e) {
			ConsoleActivity.log(e.toString());
		}
	}

	public static void registerListener(IBluetoothComms comm)
	{
		if(comms.contains(comm))
		{
			return;
		}
		comms.add(comm);
		if(socket != null && socket.isConnected())
		{
			comm.connected();//notify the listener that we are already connected.
		}
	}

	public static void unregisterListener(IBluetoothComms comm)
	{
		comms.remove(comm);
	}

	public static void getTemp()
	{
		TASK_LIST.add(()->
		{
			try {
				outputStream.write(RECEIVE_TEMP);
				int temp = inputStream.read();
				notifySetTemperature(temp);
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void getTempFloat()
	{
		TASK_LIST.add(() ->
		{
			try {
				outputStream.write(RECEIVE_TEMP_FLOAT);
				byte[] temperature = new byte[4];
				int bytesRead = 0;

				do {
					bytesRead += inputStream.read(temperature, bytesRead, temperature.length - bytesRead);
				} while (bytesRead != temperature.length);

				ByteBuffer bb = ByteBuffer.wrap(temperature);
				bb.order(ByteOrder.BIG_ENDIAN);
				float temp = bb.getFloat();
				notifySetTemperature(temp);
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void getValves()
	{
		TASK_LIST.add(()->
		{
			try {
				outputStream.write(RECEIVE_VALVE);
				//how many valves are we going to get
				int size = inputStream.read();
				int bufferSize = size * ValveOptionsData.VALVE_DATA_NETWORK_SIZE;
				byte[] bytes = new byte[bufferSize];
				int readBytes = 0;
				do {
					readBytes += inputStream.read(bytes, readBytes, bytes.length-readBytes);
				}while (readBytes == bufferSize);
				ValveGroup.groups.get(0).fromArduinoBytes(bytes);
				notifySetValves((ValveOptionsData[]) ValveGroup.groups.get(0).getValveOptionDataCollection().toArray());
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void sendValves(final ValveGroup valves)
	{
		TASK_LIST.add(()->
		{
			try {
				outputStream.write(SEND_VALVE);
				outputStream.write((byte) valves.getValveOptionDataCollection().size());
				outputStream.write(valves.toArduinoBytes());
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void sendTime(final Calendar date)
	{
		TASK_LIST.add(()->
		{
			try {
				outputStream.write(SEND_TIME);
				outputStream.write(date.get(Calendar.SECOND));
				outputStream.write(date.get(Calendar.MINUTE));
				outputStream.write(date.get(Calendar.HOUR));
				outputStream.write(date.get(Calendar.DAY_OF_WEEK));
				outputStream.write(date.get(Calendar.DAY_OF_MONTH));
				outputStream.write(date.get(Calendar.MONTH));
				outputStream.write(date.get((Calendar.YEAR)-2000));//arduino uses years from 0-99
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});

	}

	public static void getTime()
	{
		TASK_LIST.add(()->
		{
			try {
				outputStream.write(RECEIVE_TIME);
				Calendar date = new GregorianCalendar();
				date.setFirstDayOfWeek(Calendar.SUNDAY);
				outputStream.write(RECEIVE_TIME);
				date.set(Calendar.SECOND, inputStream.read());
				date.set(Calendar.MINUTE, inputStream.read());
				date.set(Calendar.HOUR, inputStream.read());
				date.set(Calendar.DAY_OF_WEEK, inputStream.read());
				date.set(Calendar.DAY_OF_MONTH, inputStream.read());
				date.set(Calendar.MONTH, inputStream.read());
				date.set(Calendar.YEAR, inputStream.read());
				notifySetTime(date);
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	private static void notifyConnected()
	{
		for(IBluetoothComms comm : comms)
		{
			comm.connected();
		}
	}

	private static void notifyConnectionFailed()
	{
		for(IBluetoothComms comm : comms)
		{
			comm.connectionFailed();
		}
	}

	private static void notifyDisconnected()
	{
		for(IBluetoothComms comm : comms)
		{
			comm.disconnected();
		}
	}

	private static void notifySetTemperature(float temperature)
	{
		for(IBluetoothComms comm : comms)
		{
			comm.setTemperature(temperature);
		}
	}

	private static void notifySetTime(Calendar time)
	{
		for(IBluetoothComms comm : comms)
		{
			comm.setTime(time);
		}
	}

	private static void notifySetValves(ValveOptionsData[] valves)
	{
		for(IBluetoothComms comm : comms)
		{
			comm.setValves(valves);
		}
	}
}
