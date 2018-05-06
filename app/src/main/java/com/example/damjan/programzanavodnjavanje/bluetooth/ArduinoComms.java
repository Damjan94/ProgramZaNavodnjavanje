package com.example.damjan.programzanavodnjavanje.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ArduinoComms extends Thread
{
	enum State
	{
		DO_NOTHING,
		RECEIVE_TEMPERATURE,
		RECEIVE_TEMPERATURE_FLOAT,
		RECEIVE_TIME,
		RECEIVE_VALVES
	}
	private State state;
	private final static byte SEND_VALVE = 0x2c;
	private final static byte RECEIVE_VALVE = 0x1c;

	private final static byte RECEIVE_TEMP = 0x2b;
	private final static byte RECEIVE_TEMP_FLOAT = 0x1b;

	private final static byte SEND_TIME = 0x2a;
	private final static byte RECEIVE_TIME = 0x1a;

	private final InputStream m_inputStream;
	private final OutputStream m_outputStream;

	private final IBluetoothComms m_comms;
	private final BluetoothSocket m_socket;
	public ArduinoComms(BluetoothSocket socket, IBluetoothComms comms) throws IOException
	{
		m_socket = socket;
		m_inputStream = m_socket.getInputStream();
		m_outputStream = m_socket.getOutputStream();

		m_comms = comms;
		state = State.DO_NOTHING;
	}

	@Override
	public void run()
	{
		while(m_socket.isConnected()) {
			//todo: synchronize on something, to prevent state from being changed in other threads
			try {
				switch (state) {
					case RECEIVE_TEMPERATURE: {
						m_comms.setTemperature(getTempAsync());
						state = State.DO_NOTHING;
						break;
					}
					case RECEIVE_TEMPERATURE_FLOAT:
					{
						m_comms.setTemperature(getTempFloatAsync());
					}
					case RECEIVE_VALVES: {
						m_comms.setValves(getValvesAsync());
						state = State.DO_NOTHING;
						break;
					}
					case RECEIVE_TIME: {
						m_comms.setTime(getTimeAsync());
						state = State.DO_NOTHING;
						break;
					}
					case DO_NOTHING://pass trough
					default: {
						try {
							synchronized (this)
							{
								this.wait();
							}
						} catch (InterruptedException e) {
							Log.e("bluetooth connect thread", e.toString());
						}
					}
				}
			} catch (IOException e) {
				Log.e("bluetooth connect thread", e.toString());
			}
		}
	}

	public void getTemp() throws IOException
	{
		if(/*something*/true)
		{
			m_outputStream.write(RECEIVE_TEMP);
			state = State.RECEIVE_TEMPERATURE;
			synchronized (this)
			{
				this.notify();
			}
		}
	}

	private int getTempAsync() throws IOException
	{
		return m_inputStream.read();
	}

	public void getTempFloat() throws IOException
	{
		if(/*something*/true)
		{
			m_outputStream.write(RECEIVE_TEMP_FLOAT);
			state = State.RECEIVE_TEMPERATURE_FLOAT;
			synchronized (this)
			{
				this.notify();
			}
		}
	}

	private float getTempFloatAsync() throws IOException
	{
		byte[] temperature = new byte[4];
		int bytesRead = 0;

		do {
			bytesRead += m_inputStream.read(temperature, bytesRead, temperature.length-bytesRead);
		}while(bytesRead != temperature.length);

		ByteBuffer bb = ByteBuffer.wrap(temperature);
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getFloat();
	}

	public void getValves() throws IOException
	{
		if(/*something*/true)
		{
			m_outputStream.write(RECEIVE_VALVE);
			state = State.RECEIVE_VALVES;
			synchronized (this)
			{
				this.notify();
			}
		}
	}

	private ValveOptionsData[] getValvesAsync() throws IOException
	{
		m_outputStream.write(RECEIVE_VALVE);

		int size = m_inputStream.read();
		ValveOptionsData[] data = new ValveOptionsData[size];
		for(int i = 0; i < size; i++)
		{
			byte[] valveFromArduino = new byte[ValveOptionsData.VALVE_DATA_NETWORK_SIZE];
			for (int j = 0; j < valveFromArduino.length; j++)
			{
				valveFromArduino[j] = (byte)m_inputStream.read();
			}

			int valveNumber = valveFromArduino[0];
			byte hour = valveFromArduino[1];
			byte minute = valveFromArduino[2];

			//arduino sends days on as a byte
			//with most significant bit being saturday
			//second to last(least) significant(8-7) bit being sunday
			//the least significant bit is not used
			byte daysOn = valveFromArduino[3];
			boolean[] repeatDays = new boolean[7];
			for(int k = 0; k< repeatDays.length; k++)
			{
				repeatDays[k] = ((daysOn >> k+1) & 0x1) == 1;
			}

			ByteBuffer bb = ByteBuffer.allocate(2);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(valveFromArduino[4]);
			bb.put(valveFromArduino[5]);
			short timeCountdown = bb.getShort(0);

			data[i] = new ValveOptionsData(
					MainActivity.mainActivity.getResources().getString(R.string.valve) + valveNumber,
					valveNumber,
					100,
					hour,
					minute,
					timeCountdown,
					repeatDays,
					false
			);
		}
		return data;
	}

	public void sendValves(ValveOptionsData[] valves) throws IOException
	{

		m_outputStream.write(SEND_VALVE);
		m_outputStream.write((byte)valves.length);
		for(ValveOptionsData data : valves)
		{
			byte[] arr = data.getBytesForArduino();
			for (byte anArr : arr)
			{
				m_outputStream.write(anArr);
			}
		}
	}

	private void sendTime(Calendar date) throws IOException
	{
		m_outputStream.write(SEND_TIME);
		m_outputStream.write(date.get(Calendar.SECOND));
		m_outputStream.write(date.get(Calendar.MINUTE));
		m_outputStream.write(date.get(Calendar.HOUR));
		m_outputStream.write(date.get(Calendar.DAY_OF_WEEK));
		m_outputStream.write(date.get(Calendar.DAY_OF_MONTH));
		m_outputStream.write(date.get(Calendar.MONTH));
		m_outputStream.write(date.get((Calendar.YEAR)-2000));//arduino uses years from 0-99
	}

	public void getTime() throws IOException
	{
		if(/*something*/true)
		{
			m_outputStream.write(RECEIVE_TIME);
			state = State.RECEIVE_TIME;
			synchronized (this)
			{
				this.notify();
			}
		}
	}

	private Calendar getTimeAsync() throws IOException
	{
		Calendar date = new GregorianCalendar();
		date.setFirstDayOfWeek(Calendar.SUNDAY);
		m_outputStream.write(RECEIVE_TIME);
		date.set(Calendar.SECOND, m_inputStream.read());
		date.set(Calendar.MINUTE, m_inputStream.read());
		date.set(Calendar.HOUR, m_inputStream.read());
		date.set(Calendar.DAY_OF_WEEK, m_inputStream.read());
		date.set(Calendar.DAY_OF_MONTH, m_inputStream.read());
		date.set(Calendar.MONTH, m_inputStream.read());
		date.set(Calendar.YEAR, m_inputStream.read());

		return date;
	}
}
