package com.example.damjan.programzanavodnjavanje.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class MyCrc32 extends CRC32 implements CustomSerialization
{
	public MyCrc32()
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
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt((int)getValue());
		bb.order(ByteOrder.BIG_ENDIAN);
		byte[] bytes = new byte[4];
		bb.get(bytes, 0, 4);
		return bytes;
	}

	@Override
	public void fromArduinoBytes(byte[] bytes) throws InvalidObjectException
	{
		throw new UnsupportedOperationException();
	}

	public static long convert(byte[] rawCRC32)
	{
		ByteBuffer bb = ByteBuffer.allocate(8);//ByteBuffer.wrap(rawCRC32);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putLong(0L);
		bb.put(rawCRC32);
		bb.rewind();
		return bb.getLong();
	}
}
