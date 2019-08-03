package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.data.bluetooth.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MyCalendar extends GregorianCalendar implements NetworkSerializable, JsonSerializable
{
    final static int NETWORK_SIZE = 7;

    private final static String SECOND_STRING       = "Second";
    private final static String MINUTE_STRING       = "Minute";
    private final static String HOUR_OF_DAY_STRING  = "Hour of day";
    private final static String DAY_OF_WEEK_STRING  = "Day of week";
    private final static String DAY_OF_MONTH_STRING = "Day of month";
    private final static String MONTH_STRING        = "Month";
    private final static String YEAR_STRING         = "Year";

    public MyCalendar() { super(); }

    MyCalendar(JSONObject jsonObject) throws JSONException
    {
        this();
        fromJSON(jsonObject);
    }

    @Override
    public Message toMessage()
    {
        Message msg = new Message(Message.Type.COMMAND, Message.Action.TIME, (byte) MyCalendar.NETWORK_SIZE);

        msg.set(0, (byte) (this.get(Calendar.SECOND)));
        msg.set(1, (byte) (this.get(Calendar.MINUTE)));
        msg.set(2, (byte) (this.get(Calendar.HOUR_OF_DAY)));
        msg.set(3, (byte) (this.get(Calendar.DAY_OF_WEEK)));
        msg.set(4, (byte) (this.get(Calendar.DAY_OF_MONTH)));
        msg.set(5, (byte) (this.get(Calendar.MONTH)));
        msg.set(6, (byte) (this.get(Calendar.YEAR) - 2000));//arduino uses years from 0-99

        if (msg.at(6) == 0)
        {
            throw new IllegalStateException("arduino treats year 0 as an invalid date");
        }

        return msg;
    }

    @Override
    public void fromMessage(Message message)
    {
        this.set(Calendar.SECOND,       message.at(0));
        this.set(Calendar.MINUTE,       message.at(1));
        this.set(Calendar.HOUR_OF_DAY,  message.at(2));
        this.set(Calendar.DAY_OF_WEEK,  message.at(3));
        this.set(Calendar.DAY_OF_MONTH, message.at(4));
        this.set(Calendar.MONTH,        message.at(5));
        this.set(Calendar.YEAR,         message.at(6) + 2000);//arduino uses years from 0-99
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject data = new JSONObject();
        data.put(SECOND_STRING,         this.get(Calendar.SECOND));
        data.put(MINUTE_STRING,         this.get(Calendar.MINUTE));
        data.put(HOUR_OF_DAY_STRING,    this.get(Calendar.HOUR_OF_DAY));
        data.put(DAY_OF_WEEK_STRING,    this.get(Calendar.DAY_OF_WEEK));
        data.put(DAY_OF_MONTH_STRING,   this.get(Calendar.DAY_OF_MONTH));
        data.put(MONTH_STRING,          this.get(Calendar.MONTH));
        data.put(YEAR_STRING,           this.get(Calendar.YEAR));

        return data;
    }

    @Override
    public void fromJSON(JSONObject jsonIn) throws JSONException
    {
        this.set(Calendar.SECOND,       jsonIn.getInt(SECOND_STRING));
        this.set(Calendar.MINUTE,       jsonIn.getInt(MINUTE_STRING));
        this.set(Calendar.HOUR_OF_DAY,  jsonIn.getInt(HOUR_OF_DAY_STRING));
        this.set(Calendar.DAY_OF_WEEK,  jsonIn.getInt(DAY_OF_WEEK_STRING));
        this.set(Calendar.DAY_OF_MONTH, jsonIn.getInt(DAY_OF_MONTH_STRING));
        this.set(Calendar.MONTH,        jsonIn.getInt(MONTH_STRING));
        this.set(Calendar.YEAR,         jsonIn.getInt(YEAR_STRING));
    }
}
