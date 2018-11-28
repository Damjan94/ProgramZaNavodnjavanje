package com.example.damjan.programzanavodnjavanje.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MyCalendar extends GregorianCalendar implements CustomSerialization
{
	public final static int NETWORK_SIZE = 7;
	public MyCalendar()
	{
		super();
	}

	@Override
	public JSONObject toJson() throws JSONException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void fromJSON(JSONObject jsonIn) throws JSONException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduinoBytes()
	{
		byte[] date = new byte[NETWORK_SIZE];

		date[0] = (byte)(this.get(Calendar.SECOND));
		date[1] = (byte)(this.get(Calendar.MINUTE));
		date[2] = (byte)(this.get(Calendar.HOUR));
		date[3] = (byte)(this.get(Calendar.DAY_OF_WEEK));
		date[4] = (byte)(this.get(Calendar.DAY_OF_MONTH));
		date[5] = (byte)(this.get(Calendar.MONTH));
		date[6] = (byte)(this.get(Calendar.YEAR)-2000);//arduino uses years from 0-99
		return date;
	}

	@Override
	public void fromArduinoBytes(byte[] bytes) throws InvalidObjectException
	{
		if(bytes.length != NETWORK_SIZE)
			throw new InvalidObjectException("the provided byte buffer is not 6 bytes");

		this.set(Calendar.SECOND, 		bytes[0]);
		this.set(Calendar.MINUTE, 		bytes[1]);
		this.set(Calendar.HOUR, 		bytes[2]);
		this.set(Calendar.DAY_OF_WEEK, 	bytes[3]);
		this.set(Calendar.DAY_OF_MONTH, bytes[4]);
		this.set(Calendar.MONTH, 		bytes[5]);
		this.set(Calendar.YEAR, 		bytes[6]+2000);//arduino uses years from 0-99
	}
}
