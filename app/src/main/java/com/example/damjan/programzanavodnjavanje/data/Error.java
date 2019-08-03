package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.data.bluetooth.Message;

import org.json.JSONException;
import org.json.JSONObject;

public class Error implements NetworkSerializable, JsonSerializable
{

    private static final String ERROR_NUMBER_STRING = "Error number";
    private static final String DATE_TIME_STRING    = "Date&time";
    public Error(JSONObject obj) throws JSONException
    {
        fromJSON(obj);
    }

    public Error(Message message)
    {
        fromMessage(message);
    }

    private MyCalendar m_sleepDate;
    private Error.Number m_number;

    @Override
    public Message toMessage()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fromMessage(Message message)
    {
        m_sleepDate.fromMessage(message);
        m_number = Error.Number.get(message.at(MyCalendar.NETWORK_SIZE));

    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject data = new JSONObject();
        data.put(ERROR_NUMBER_STRING, m_number.toByte());
        data.put(DATE_TIME_STRING, m_sleepDate.toJson());

        return data;
    }

    @Override
    public void fromJSON(JSONObject jsonIn) throws JSONException
    {
        m_number.m_value    = (byte)jsonIn.getInt(ERROR_NUMBER_STRING);
        m_sleepDate         = new MyCalendar(jsonIn.getJSONObject(DATE_TIME_STRING));
    }

    //TODO override toString
    public enum Number
    {
        NONE                        ((byte)0x0),
        STARTED_IGNORING_THE_DEVICE ((byte)0x1),
        INVALID_CRC                 ((byte)0x2),
        COULD_NOT_READ_ALL_BYTES    ((byte)0x3),
        TOO_MANY_VALVES_TO_RECEIVE  ((byte)0x4),
        INVALID_MESSAGE_PROTOCOL    ((byte)0xFF);

        byte m_value;

        Number(byte b)
        {
            m_value = b;
        }

        public static Error.Number get(byte error)
        {
            if (error == STARTED_IGNORING_THE_DEVICE.m_value)
            {
                return STARTED_IGNORING_THE_DEVICE;
            } else if (error == INVALID_CRC.m_value)
            {
                return INVALID_CRC;
            } else if (error == COULD_NOT_READ_ALL_BYTES.m_value)
            {
                return COULD_NOT_READ_ALL_BYTES;
            } else if (error == TOO_MANY_VALVES_TO_RECEIVE.m_value)
            {
                return TOO_MANY_VALVES_TO_RECEIVE;
            } else if (error == INVALID_MESSAGE_PROTOCOL.m_value)
            {
                return INVALID_MESSAGE_PROTOCOL;
            } else
            {
                return NONE;
            }
        }

        public byte toByte() { return m_value; }
    }
}
