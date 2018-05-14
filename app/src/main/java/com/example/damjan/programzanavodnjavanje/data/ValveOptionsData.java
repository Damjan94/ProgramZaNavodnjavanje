package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.PatternSyntaxException;

public class ValveOptionsData implements CustomSerialization{
    
    private static final String VALVE_NAME = "Name";
    private static final String VALVE_NUMBER = "Number";
    private static final String VALVE_PERCENT = "Percent";
    private static final String VALVE_COUNTDOWN = "Countdown";
    private static final String VALVE_MINUTE= "Minute";
    private static final String VALVE_HOUR = "Hour";
    private static final String VALVE_SWITCH = "Switch";
    private static final String VALVE_DAYS_ON= "DaysOn";
    


    private static long staticId = 0;
	
	public final static int VALVE_DATA_NETWORK_SIZE = 6;

	private final static int DEFAULT_PERCENTAGE = 100;

	//used by RecyclerView.Adapter.setHasStableIds(true)
    private long m_id = staticId++;

    private String m_valveName;
    private int m_valveNumber;
    private int m_hour;
    private int m_minute;
    private int m_timeCountdown;

    private boolean m_masterSwitch;

    private int m_percentage;
    private boolean[] m_repeatDays;

	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject jsonOut = new JSONObject();
		jsonOut.put(VALVE_NAME, m_valveName);
		jsonOut.put(VALVE_NUMBER, m_valveNumber);
		jsonOut.put(VALVE_PERCENT, m_percentage);
		jsonOut.put(VALVE_COUNTDOWN, m_timeCountdown);
		jsonOut.put(VALVE_MINUTE, m_minute);
		jsonOut.put(VALVE_HOUR, m_hour);
		jsonOut.put(VALVE_SWITCH, m_masterSwitch);

		JSONArray daysOn = new JSONArray(m_repeatDays);
		jsonOut.put(VALVE_DAYS_ON, daysOn);
		return jsonOut;
	}

	@Override
	final public void fromJSON(JSONObject jsonIn) throws JSONException
	{
		JSONArray daysOn = jsonIn.getJSONArray(VALVE_DAYS_ON);
		boolean[] days = new boolean[daysOn.length()];
		for (int i = 0; i < days.length; i++)
		{
			days[i] = daysOn.getBoolean(i);
		}

		this.m_valveName = jsonIn.getString(VALVE_NAME);
		this.m_valveNumber = jsonIn.getInt(VALVE_NUMBER);
		this.m_percentage = jsonIn.getInt(VALVE_PERCENT);
		this.m_hour = jsonIn.optInt(VALVE_HOUR);
		this.m_minute = jsonIn.getInt(VALVE_MINUTE);
		this.m_timeCountdown = jsonIn.getInt(VALVE_COUNTDOWN);
		this.m_repeatDays = days;
		this.m_masterSwitch = jsonIn.getBoolean(VALVE_SWITCH);
	}

	@Override
	public byte[] toArduinoBytes()
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

		arr[3] = (byte) daysOn;
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(m_timeCountdown);
		byte[] timeCountdown = bb.array();
		arr[4] = timeCountdown[0];
		arr[5] = timeCountdown[1];
		return arr;
	}

	@Override
	final public void fromArduinoBytes(final byte[] bytes)
	{
		int valveNumber = bytes[0];
		byte hour = bytes[1];
		byte minute = bytes[2];

		//arduino sends days on as a byte
		//with most significant bit being saturday
		//second to last(least) significant(8-7) bit being sunday
		//the least significant bit is not used
		byte daysOn = bytes[3];
		boolean[] repeatDays = new boolean[7];
		for(int k = 0; k< repeatDays.length; k++)
		{
			repeatDays[k] = ((daysOn >> k+1) & 0x1) == 1;
		}

		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.put(bytes[4]);
		bb.put(bytes[5]);
		short timeCountdown = bb.getShort(0);

		this.m_valveNumber = valveNumber;
		this.m_hour = hour;
		this.m_minute =minute;
		this.m_repeatDays = repeatDays;
		this.m_timeCountdown = timeCountdown;
		this.m_percentage = DEFAULT_PERCENTAGE;
		this.m_masterSwitch = false;
	}


    public ValveOptionsData()
	{
		this(
				MainActivity.mainActivity.getResources().getString(R.string.valve) + staticId,
				(int) staticId,
				DEFAULT_PERCENTAGE,
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
    }

	ValveOptionsData(JSONObject obj) throws JSONException
	{
		fromJSON(obj);
	}

	ValveOptionsData(byte[] bytes)
	{
		fromArduinoBytes(bytes);
	}

	public int getPercentage() {
		return m_percentage;
	}
	public void setPercentage(int percentage)
	{
		this.m_percentage = Math.max(0, percentage);
	}

    public void setValveName(String valveName) {
        this.m_valveName = valveName;
    }
	public String getValveName() {
		return m_valveName;
	}

    public void setValveNumber(int valveNumber) {
        this.m_valveNumber = valveNumber;
    }
	public int getValveNumber() {
		return m_valveNumber;
	}

    public void setTime(int hour, int minute)
    {
        this.m_hour = hour;
        this.m_minute = minute;
    }
	public int getMinutes(){ return m_minute; }
	public int getHours(){ return m_hour; }


    public void setTimeCountdown(int timeCountdown)
	{
        this.m_timeCountdown = timeCountdown;
    }
	public int getTimeCountdown()
	{
		//guard from division by zero
		if(m_timeCountdown == 0)
		{
			return m_timeCountdown;
		}
		return (int)((m_timeCountdown / 100.0) * m_percentage);
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
    public boolean[] getRepeatDays() {
        return m_repeatDays;
    }

	public void setMasterSwitch(boolean masterSwitch) {
		this.m_masterSwitch = masterSwitch;
	}
	public boolean isMasterSwitch() {
		return m_masterSwitch;
	}


    public long getID() {
        return m_id;
    }

    @Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Valve:\n");

		sb.append("Valve Name: ");
		sb.append(m_valveName);
		sb.append('\n');

		sb.append("Valve Number: ");
		sb.append(m_valveNumber);
		sb.append('\n');

		sb.append("Percentage: ");
		sb.append(m_percentage);
		sb.append('\n');

		sb.append("Turn on hour: ");
		sb.append(m_hour);
		sb.append('\n');

		sb.append("Turn on minute: ");
		sb.append(m_minute);
		sb.append('\n');

		sb.append("Countdown Time: ");
		sb.append(m_timeCountdown);
		sb.append('\n');

		sb.append("Days On: ");
		for(int i = 0; i < m_repeatDays.length; i++)
		{
			if(m_repeatDays[i])
			{
				switch(i)
				{
					case 0: {
						sb.append("Sunday, ");
						break;
					}
					case 1: {
						sb.append("Monday, ");
						break;
					}
					case 2: {
						sb.append("Tuesday, ");
						break;
					}
					case 3: {
						sb.append("Wednesday, ");
						break;
					}
					case 4: {
						sb.append("Thursday, ");
						break;
					}
					case 5: {
						sb.append("Friday, ");
						break;
					}
					case 6: {
						sb.append("Saturday, ");
						break;
					}

				}
			}
		}

		sb.delete(sb.length()- 2, sb.length());

		sb.append("\nSwitch: ");
		sb.append(m_masterSwitch);
		sb.append('\n');

		return sb.toString();
	}
	
}
