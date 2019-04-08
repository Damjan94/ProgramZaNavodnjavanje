package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.utility.Network;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class MyCrc32 extends CRC32
{
    public static final int NETWORK_SIZE = 4;

    public MyCrc32()
    {
        super();
    }

    public static long read(InputStream inputStream) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(MyCrc32.NETWORK_SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        Network.read(inputStream, buffer.array(), "MyCrc32");
        return buffer.getInt();
    }

    public static int calculate(byte[] buf)
    {
        CRC32 crc = new CRC32();
        crc.update(buf);
        return (int) crc.getValue();
    }

    public byte[] toBytes()
    {
        ByteBuffer bb = ByteBuffer.allocate(NETWORK_SIZE);
        bb.putInt((int) getValue());
        bb.rewind();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.array();
    }

    public long fromBytes(final byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, 0, NETWORK_SIZE);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }
	/*
	public static byte[] calculateCrcAndCombine(byte[] toCalculate)
	{
		MyCrc32 crc = new MyCrc32();
		crc.update(toCalculate);
		byte[] crcBytes = crc.toArduinoBytes();
		byte[] combined = new byte[toCalculate.length + crcBytes.length];

		System.arraycopy(crcBytes, 0, combined, 0, crcBytes.length);
		System.arraycopy(toCalculate, 0, combined, crcBytes.length, toCalculate.length);

		return combined;
	}
	*/
}
