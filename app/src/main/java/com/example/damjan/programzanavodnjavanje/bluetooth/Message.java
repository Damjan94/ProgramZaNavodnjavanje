package com.example.damjan.programzanavodnjavanje.bluetooth;

import com.example.damjan.programzanavodnjavanje.data.MyCrc32;
import com.example.damjan.programzanavodnjavanje.utility.Network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Message
{
    static final int NETWORK_SIZE = 5;
    static final byte PROTOCOL_VERSION = 0;
    private Type m_type;
    private Action m_action;
    private Info m_info;
    private byte[] m_data;

    Message()
    {
        this(Type.NONE, Action.NONE, Info.NONE, (byte) 0);
    }

    Message(Type type, Action action)
    {
        this(type, action, Info.NONE, (byte) 0);
    }

    public Message(Type type, Action action, byte size) { this(type, action, Info.NONE, size); }

    Message(Type type, Info info)
    {
        this(type, Action.NONE, info, (byte) 0);
    }

    Message(Type type, Action action, Info info, byte size)
    {
        this.m_type = type;
        this.m_action = action;
        this.m_info = info;
        m_data = new byte[size];
    }

    //public Message(final byte[] arduinoBytes) {fromBytes(arduinoBytes);}

    public byte[] toBytes()
    {
        byte[] header = new byte[Message.NETWORK_SIZE];
        header[0] = Message.PROTOCOL_VERSION;
        header[1] = m_type.toByte();
        header[2] = m_action.toByte();
        header[3] = m_info.toByte();
        header[4] = (byte) m_data.length;

        MyCrc32 headerCrc = new MyCrc32();
        headerCrc.update(header);

        int bufferSize = MyCrc32.NETWORK_SIZE + Message.NETWORK_SIZE;
        if (m_data.length > 0)
        {
            bufferSize = MyCrc32.NETWORK_SIZE + Message.NETWORK_SIZE + MyCrc32.NETWORK_SIZE + m_data.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(headerCrc.toBytes());
        buffer.put(header);

        if (m_data.length > 0)
        {
            MyCrc32 dataCrc = new MyCrc32();
            dataCrc.update(m_data);
            buffer.put(dataCrc.toBytes());
            buffer.put(m_data);
        }

        return buffer.array();
    }

    /*
        void fromBytes(final byte[] arduinoBytes)
        {
            ByteBuffer buf = ByteBuffer.wrap(arduinoBytes);
            long receivedCRC = buf.getInt();
            byte protocolVersion = buf.get();

            this.m_type		= Type.get(buf.get());
            this.m_action	= Action.fromByte(buf.get());
            this.m_info		= Info.get(buf.get());

            int size		= buf.get();
            if(arduinoBytes.length != size + MyCrc32.NETWORK_SIZE)
            {
                throw new RuntimeException("byte length and size are not equal");
            }
            this.m_data = new byte[size];
            System.arraycopy(arduinoBytes, MyCrc32.NETWORK_SIZE + Message.NETWORK_SIZE, this.m_data, 0, this.m_data.length);

            CRC32 crc32 = new CRC32();
            crc32.update(arduinoBytes, MyCrc32.NETWORK_SIZE, arduinoBytes.length - MyCrc32.NETWORK_SIZE);//we don't want the first 4 bytes, that's the received crc
            if(crc32.getValue() != receivedCRC)
                throw new RuntimeException("CRC missmetch");
        }
    */
    public void set(int index, byte data)
    {
        m_data[index] = data;
    }

    public void set(int index, byte[] data)
    {
        System.arraycopy(data, 0, m_data, index, data.length);
    }

    public void write(OutputStream outputStream) throws IOException
    {
        outputStream.write(toBytes());
        outputStream.flush();
    }

    public void read(InputStream inputStream) throws IOException
    {
        long headerCrc = MyCrc32.read(inputStream);
        byte[] msgHeaderBuffer = new byte[Message.NETWORK_SIZE];

        Network.read(inputStream, msgHeaderBuffer, this);
        long calculatedHeaderCrc = MyCrc32.calculate(msgHeaderBuffer);
        if (headerCrc != calculatedHeaderCrc) throw new RuntimeException("[Message] CRC mismatch");

        ByteBuffer buf = ByteBuffer.wrap(msgHeaderBuffer);
        byte protocolVersion = buf.get();//TODO set the protocol version to be used in the future, if supported
        this.m_type = Type.get(buf.get());
        this.m_action = Action.fromByte(buf.get());
        this.m_info = Info.get(buf.get());
        short size = buf.get();//the fact that size is transmitted as a byte,
        // ensures us that we wont have any large allocation requests(should there be any malformed packets)

        byte[] dataBuffer = new byte[size];
        if (size > 0)
        {
            long dataCrc = MyCrc32.read(inputStream);
            Network.read(inputStream, dataBuffer, this);

            long calculatedDataCrc = MyCrc32.calculate(dataBuffer);
            if (calculatedDataCrc != dataCrc)
                throw new RuntimeException("[Message] CRC mismatch");//is it ok to not assign dataBuffer to m_data?
        }
        m_data = dataBuffer;
    }

    public Type getType() { return m_type; }

    public Action getAction() { return m_action; }

    public Info getInfo() { return m_info; }

    public byte[] getData() { return m_data; }

    public byte at(int index) { return m_data[index]; }

    public enum Type
    {
        NONE((byte) 0x0),
        COMMAND((byte) 0x1),    //Send new valves or time to arduino
        REQUEST((byte) 0x2),    //Ask the arduino for some action
        INFO((byte) 0x3);

        final byte m_value;

        Type(byte b)
        {
            m_value = b;
        }

        public static Type get(byte type)
        {
            if (type == COMMAND.m_value)
            {
                return COMMAND;
            } else if (type == REQUEST.m_value)
            {
                return REQUEST;
            } else if (type == INFO.m_value)
            {
                return INFO;
            } else
            {
                return NONE;
            }
        }

        public byte toByte() { return m_value; }

    }

    public enum Action
    {
        NONE((byte) 0x0),
        VALVE((byte) 0x1),
        TIME((byte) 0x2),
        TEMPERATURE((byte) 0x3),
        TEMPERATURE_FLOAT((byte) 0x4),
        H_BRIDGE_PIN((byte) 0x5),
        ERROR((byte) 0x6),
        SLEEP_TIME_SHORT((byte) 0x7),
        SLEEP_TIME_LONG((byte) 0x8);

        byte m_value;

        Action(byte b)
        {
            m_value = b;
        }

        public static Action fromByte(byte action)
        {
            if (action == VALVE.m_value)
            {
                return VALVE;

            } else if (action == TIME.m_value)
            {
                return TIME;

            } else if (action == TEMPERATURE.m_value)
            {
                return TEMPERATURE;

            } else if (action == TEMPERATURE_FLOAT.m_value)
            {
                return TEMPERATURE_FLOAT;

            } else if (action == H_BRIDGE_PIN.m_value)
            {
                return H_BRIDGE_PIN;

            } else if (action == ERROR.m_value)
            {
                return ERROR;
            } else if (action == SLEEP_TIME_SHORT.m_value)
            {
                return SLEEP_TIME_SHORT;
            }  else if (action == SLEEP_TIME_LONG.m_value)
            {
                return SLEEP_TIME_LONG;
            } else
            {
                return NONE;
            }
        }

        public byte toByte() { return m_value; }

    }

    public enum Info
    {

        NONE((byte) 0x0),
        NOMINAL((byte) 0x1),
        READY_TO_SEND((byte) 0x2),
        READY_TO_RECEIVE((byte) 0x3),
        HANDSHAKE((byte) 0x4);

        final byte m_value;

        Info(byte b)
        {
            m_value = b;
        }

        public static Info get(byte info)
        {
            if (info == NOMINAL.m_value)
            {
                return NOMINAL;
            } else if (info == READY_TO_SEND.m_value)
            {
                return READY_TO_SEND;
            } else if (info == READY_TO_RECEIVE.m_value)
            {
                return READY_TO_RECEIVE;
            } else if (info == HANDSHAKE.m_value)
            {
                return HANDSHAKE;
            } else
            {
                return NONE;
            }
        }

        public byte toByte() { return m_value; }
    }
}
