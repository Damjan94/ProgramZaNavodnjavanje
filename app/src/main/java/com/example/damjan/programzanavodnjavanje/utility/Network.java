package com.example.damjan.programzanavodnjavanje.utility;

import com.example.damjan.programzanavodnjavanje.bluetooth.Message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Network
{
    private Network() {}//as this is a helper class related to network stuff(duh), no need to instantiate it

    public static float getFloat(Message msg, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(msg.getData(), offset, 4);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getFloat();
    }

    public static int getInt(Message msg, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(msg.getData(), offset, 4);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    public static short getShort(Message msg, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(msg.getData(), offset, 2);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort();
    }

    public static void read(InputStream inputStream, byte[] buffer, Object caller) throws IOException
    {
        Network.read(inputStream, buffer, caller.getClass().toString());
    }

    public static void read(InputStream inputStream, byte[] buffer, String callerName) throws IOException
    {
        int readBytes = 0;
        do
        {
            readBytes += inputStream.read(buffer, readBytes, buffer.length - readBytes);
        } while (!(readBytes >= buffer.length));
        if (readBytes != buffer.length)
            throw new IOException("[" + callerName + "] Not all bytes could be read");
    }
}
