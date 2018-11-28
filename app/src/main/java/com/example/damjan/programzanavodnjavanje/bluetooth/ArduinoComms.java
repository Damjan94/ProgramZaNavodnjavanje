package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.data.MyCalendar;
import com.example.damjan.programzanavodnjavanje.data.MyCrc32;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32;


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
			try
			{
				Message msg = new Message(Message.Type.REQUEST, Message.Action.TEMPERATURE);
				outputStream.write(msg.toArduinoBytes());

				long receivedCRC32 = MyCrc32.convert(readBytes(4));//order matters! always read the crc first!!!
				byte temp = (byte)inputStream.read();

				MyCrc32 crc32 = new MyCrc32();
				crc32.update(temp);
				if(crc32.getValue() != receivedCRC32)
				{
					ConsoleActivity.log("getTemp crc mismatch. expected "+crc32.getValue()+" got "+receivedCRC32);
				}

				notifySetTemperature(temp);
			} catch (IOException e)
			{
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void getTempFloat()
	{
		TASK_LIST.add(() ->
		{
			try {
				Message msg = new Message(Message.Type.REQUEST, Message.Action.TEMPERATURE_FLOAT);
				outputStream.write(msg.toArduinoBytes());

				long receivedCRC32 = MyCrc32.convert(readBytes(4));//order matters! always read the crc first!!!
				byte[] temperature = readBytes(4);

				CRC32 crc32 = new CRC32();
				crc32.update(temperature);
				if(crc32.getValue() != receivedCRC32)
				{
					ConsoleActivity.log("getTempFloat crc mismatch. expected "+crc32.getValue()+" got "+receivedCRC32);
				}

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
				Message msg = new Message(Message.Type.REQUEST, Message.Action.VALVE);
				outputStream.write(msg.toArduinoBytes());
				//how many valves are we going to get
				Message msgCount = new Message(readBytes(Message.NETWORK_SIZE));

				int bufferSize = msgCount.itemCount * ValveOptionsData.NETWORK_SIZE;
				byte[] bytes = readBytes(bufferSize);
				//TODO: make the following statement prettier
				//TODO: this no longer works. make changes for the crc32
				asdf
				ValveGroup.groups.get(0).fromArduinoBytes(bytes);
				notifySetValves(ValveGroup.groups.get(0).getValveOptionDataCollection().toArray(new ValveOptionsData[0]));
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void sendValves(final ValveGroup valves)
	{
		TASK_LIST.add(()->
		{
			try
			{
				Message msg = new Message(Message.Type.COMMAND, Message.Action.VALVE, (byte)valves.getValveOptionDataCollection().size());
				outputStream.write(msg.toArduinoBytes());

				for()
				msg.fromArduinoBytes(readBytes(Message.NETWORK_SIZE));
				if(msg.type == Message.Type.INFO)
				{
					if(msg.info == Message.Info.READY_TO_RECEIVE)
					{

					}
				}
				asdf

				outputStream.flush();
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void sendTime(final MyCalendar date)
	{
		TASK_LIST.add(()->
		{
			try
			{
				Message msg = new Message(Message.Type.COMMAND, Message.Action.TIME, (byte)1);
				byte[] dateArray = date.toArduinoBytes();

				MyCrc32 crc32 = new MyCrc32();
				crc32.update(dateArray);

				outputStream.write(msg.toArduinoBytes());
				outputStream.write(crc32.toArduinoBytes());
				outputStream.write(dateArray);
				outputStream.flush();
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

				Message msg = new Message(Message.Type.REQUEST, Message.Action.TIME);
				outputStream.write(msg.toArduinoBytes());

				MyCalendar date = new MyCalendar();
				date.setFirstDayOfWeek(Calendar.SUNDAY);

				notifySetTime(date);
			} catch (IOException e) {
				ConsoleActivity.log(e.toString());
			}
		});
	}

	public static void getHBridgePin()
	{
		TASK_LIST.add(()->
		{
			try {
				Message msg = new Message(Message.Type.REQUEST, Message.Action.H_BRIDGE_PIN);
				outputStream.write(msg.toArduinoBytes());
				long receivedCRC32 = MyCrc32.convert(readBytes(4));
				byte[] hbridgePin = readBytes(2);
				CRC32 crc32 = new CRC32();
				crc32.update(hbridgePin);
				if(crc32.getValue() != receivedCRC32)
				{
					ConsoleActivity.log("getHBridgePin crc mismatch. expected "+crc32.getValue()+" got "+receivedCRC32);
				}
				ConsoleActivity.log("Hbridge: "+ hbridgePin[0] + ", " + hbridgePin[1]);
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

	private static byte[] readBytes(int byteCount) throws IOException
	{
		byte[] bytes = new byte[byteCount];
		int readBytes = 0;
		do {
			readBytes += inputStream.read(bytes, readBytes, bytes.length-readBytes);
		}while (readBytes != byteCount);

		return bytes;
	}
}
