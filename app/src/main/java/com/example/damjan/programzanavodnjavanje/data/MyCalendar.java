package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.bluetooth.Message;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MyCalendar extends GregorianCalendar implements NetworkSerializable
{
    public final static int NETWORK_SIZE = 7;

    public MyCalendar() { super(); }

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
        this.set(Calendar.SECOND, message.at(0));
        this.set(Calendar.MINUTE, message.at(1));
        this.set(Calendar.HOUR_OF_DAY, message.at(2));
        this.set(Calendar.DAY_OF_WEEK, message.at(3));
        this.set(Calendar.DAY_OF_MONTH, message.at(4));
        this.set(Calendar.MONTH, message.at(5));
        this.set(Calendar.YEAR, message.at(6) + 2000);//arduino uses years from 0-99
    }
}
