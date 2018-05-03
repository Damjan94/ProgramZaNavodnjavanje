package com.example.damjan.programzanavodnjavanje.data;

import android.util.Log;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class ValveOptionsData{
    
    public static final String VALVE_NAME = "Name";
    public static final String VALVE_NUMBER = "Number";
    public static final String VALVE_PERCENT = "Percent";
    public static final String VALVE_COUNTDOWN = "Countdown";
    public static final String VALVE_MINUTE= "Minute";
    public static final String VALVE_HOUR = "Hour";
    public static final String VALVE_SWITCH = "Switch";
    public static final String VALVE_DAYS_ON= "DaysOn";
    
    public static ArrayList<ValveOptionsData> getValveOptionDataCollection()
    {
        
        return valveOptionDataCollection;
    }
    public static void setValveOptionDataCollection(ArrayList<ValveOptionsData> dataList)
	{
		synchronized (lock)
		{
			valveOptionDataCollection = dataList;
		}
	}
    
    public static ValveOptionsData getValveOptionData(int pos)
    {
		final ValveOptionsData data;
        synchronized (lock)
        {
             data =  valveOptionDataCollection.get(pos);
        }
        return data;
    }
    
    public static void addValveOptionData(ValveOptionsData data)
    {
        synchronized (lock)
        {
            valveOptionDataCollection.add(data);
        }
    }
	
	public static void removeValveOptionData(int pos)
	{
		synchronized (lock)
		{
			valveOptionDataCollection.remove(pos);
		}
	}
    
    private static ArrayList<ValveOptionsData> valveOptionDataCollection = new ArrayList<>();
    private static final Object lock = new Object();
    static
	{
        //temp code
        for (int i = 0; i < 1; i++)
        {
            valveOptionDataCollection.add(new ValveOptionsData());
        }
    }

    private static long m_staticId = 0;
	
	public final static int VALVE_DATA_NETWORK_SIZE = 6;
    
    private long m_id;

    private String m_valveName;
    private int m_valveNumber;
    private int m_hour;
    private int m_minute;
    private int m_timeCountdown;

    public boolean isMasterSwitch() {
        return m_masterSwitch;
    }

    public void setMasterSwitch(boolean masterSwitch) {
        this.m_masterSwitch = masterSwitch;
    }

    private boolean m_masterSwitch;
    public int getPercentage() {
        return m_percentage;
    }

    private int m_percentage;
    private boolean[] m_repeatDays;
	
	public JSONObject toJson()
	{
		JSONObject jsonOut = new JSONObject();
		try {
			jsonOut.put(VALVE_NAME, m_valveName);
			jsonOut.put(VALVE_NUMBER, m_valveNumber);
			jsonOut.put(VALVE_PERCENT, m_percentage);
			jsonOut.put(VALVE_COUNTDOWN, m_timeCountdown);
			jsonOut.put(VALVE_MINUTE, m_minute);
			jsonOut.put(VALVE_HOUR, m_hour);
			jsonOut.put(VALVE_SWITCH, m_masterSwitch);
			
			JSONArray daysOn = new JSONArray(m_repeatDays);
			jsonOut.put(VALVE_DAYS_ON, daysOn);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		
		
		return jsonOut;
	}
	public static ValveOptionsData fromJSON(JSONObject jsonIn)
	{
		ValveOptionsData data;
		try
		{
			JSONArray daysOn = jsonIn.getJSONArray(VALVE_DAYS_ON);
			boolean[] days = new boolean[daysOn.length()];
			for (int i = 0; i < days.length; i++)
			{
				days[i] = daysOn.getBoolean(i);
			}
			data = new ValveOptionsData(
					jsonIn.getString(VALVE_NAME),
					jsonIn.getInt(VALVE_NUMBER),
					jsonIn.getInt(VALVE_PERCENT),
					jsonIn.optInt(VALVE_HOUR),
					jsonIn.getInt(VALVE_MINUTE),
					jsonIn.getInt(VALVE_COUNTDOWN),
					days,
					jsonIn.getBoolean(VALVE_SWITCH)
			);
		} catch (JSONException e)
		{
			Log.e("ValveData", "Failed to load data from json\n"+jsonIn.toString());
			data = new ValveOptionsData();
		}
		return data;
	}
	public byte[] getBytesForArduino()
	{
		byte[] arr = new byte[VALVE_DATA_NETWORK_SIZE];
		arr[0] = (byte)m_valveNumber;
		arr[1] = (byte)m_hour;
		arr[2] = (byte)m_minute;
		
		int daysOn = 0;//arduino doesn't use the first bit. it uses bits from 1 - 7 sun-sat

        for(int i = 0; i < m_repeatDays.length; i++)
        {
            if(m_repeatDays[i])
            {
                daysOn |= (0x1 << i+1);
            }
        }
		/*
		//least significant bit is saturday, most significant is sunday
        for (boolean m_repeatDay : m_repeatDays)
		{
			daysOn = daysOn << 1;
			if (m_repeatDay)
			{
				daysOn |= 1;
			}
		}
        daysOn = daysOn << 1;
		*/

		arr[3] = (byte) daysOn;
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(m_timeCountdown);
		byte[] timeCountdown = bb.array();
		arr[4] = timeCountdown[0];
		arr[5] = timeCountdown[1];
		return arr;
	}
    public ValveOptionsData()
	{
		this(
				MainActivity.mainActivity.getResources().getString(R.string.valve) + m_staticId,
				(int)m_staticId,
				100,//default percentage
				0,
				0,
				0,
				new boolean[]{false, false, false, false, false, false, false},
				false
				);
	}


    public ValveOptionsData(String valveName, int valveNumber, int percentage, int hour, int minute, int timeCountdown, boolean[] repeatDays, boolean masterSwitch) {
        setValveName(valveName);
        setValveNumber(valveNumber);
        setPercentage(percentage);
        setTimeCountdown(timeCountdown);
        setTime(hour, minute);
        setMasterSwitch(masterSwitch);
        setRepeatDay(repeatDays);

        m_id = m_staticId;
        m_staticId++;
    }

    public void setValveName(String valveName) {
        this.m_valveName = valveName;
    }

    public void setValveNumber(int valveNumber) {
        this.m_valveNumber = valveNumber;
    }

    public void setTime(int hour, int minute)
    {
        this.m_hour = hour; this.m_minute = minute;
    }

    public void setPercentage(int percentage)
	{
        this.m_percentage = Math.max(0, percentage);
    }

    public void setTimeCountdown(int timeCountdown)
	{
        this.m_timeCountdown = timeCountdown;
    }

    public void setRepeatDay(boolean[] repeatDays) {
        if(repeatDays.length != 7)
            throw new PatternSyntaxException("Expected array of 7 booleans","got array of " + repeatDays.length+" booleans", -1);
        this.m_repeatDays = repeatDays;
    }

    public void setRepeatDay(boolean value, int pos)
    {
        m_repeatDays[pos] = value;
    }

    public String getValveName() {
        return m_valveName;
    }

    public int getValveNumber() {
        return m_valveNumber;
    }

    public int getMinutes(){ return m_minute; }

    public int getHours(){ return m_hour; }

    public int getTimeCountdown()
    {
        if(m_timeCountdown == 0)
        {
            return m_timeCountdown;
        }
        return (int)((m_timeCountdown / 100.0) * m_percentage);
    }

    public boolean[] getRepeatDays() {
        return m_repeatDays;
    }

    public long getID() {
        return m_id;
    }
	
}
