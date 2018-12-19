package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.PatternSyntaxException;
import java.util.zip.CRC32;

public class ValveOptionsData implements CustomSerialization
{
    //JSON serialization strings
    private static final String VALVE_NAME = "Name";
    private static final String VALVE_NUMBER = "Number";
    private static final String VALVE_COUNTDOWN = "Countdown";
    private static final String VALVE_MINUTE= "Minute";
    private static final String VALVE_HOUR = "Hour";
    private static final String VALVE_SWITCH = "Switch";
    private static final String VALVE_DAYS_ON= "DaysOn";
    
    //static ID used for RecyclerView adapter
    private static long staticId = 0;
	
	public final static int NETWORK_SIZE = 6;

	//used by RecyclerView.Adapter.setHasStableIds(true)
    private long m_id = staticId++;

    private String m_valveName;
    private short m_valveNumber;
    private byte m_hour;
    private byte m_minute;
    private int m_timeCountdown;

    private boolean m_masterSwitch;

	private boolean[] m_repeatDays;

	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject jsonOut = new JSONObject();
		jsonOut.put(VALVE_NAME, m_valveName);
		jsonOut.put(VALVE_NUMBER, m_valveNumber);
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
		this.m_valveNumber = (short)jsonIn.getInt(VALVE_NUMBER);
		this.m_hour = (byte)jsonIn.getInt(VALVE_HOUR);
		this.m_minute = (byte)jsonIn.getInt(VALVE_MINUTE);
		this.m_timeCountdown = jsonIn.getInt(VALVE_COUNTDOWN);
		this.m_repeatDays = days;
		this.m_masterSwitch = jsonIn.getBoolean(VALVE_SWITCH);
	}

	@Override
	public byte[] toArduinoBytes()
	{
		byte[] arr = new byte[NETWORK_SIZE];
		arr[0] = (byte)m_valveNumber;
		arr[1] = m_hour;
		arr[2] = m_minute;
		
		int daysOn = 0;//arduino doesn't use the first bit. it uses bits from 1 - 7 sun-sat

        for(int i = 0; i < m_repeatDays.length; i++)
        {
            if(m_repeatDays[i])
            {
                daysOn |= (0x1 << i+1);
            }
        }

		arr[3] = (byte) daysOn;
		arr[4] = (byte) (m_timeCountdown >> 8);//is this ok? is int treated as short?!
		arr[5] = (byte) m_timeCountdown;

		return MyCrc32.calculateCrcAndCombine(arr);
	}

	@Override
	final public void fromArduinoBytes(final byte[] bytes, long crc32) throws InvalidObjectException
	{
		CRC32 crc = new CRC32();
		crc.update(bytes);
		if(crc.getValue() != crc32)
		{
			throw new InvalidObjectException("Valve crc mismatch");
		}

		byte valveNumber = bytes[0];
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
		int timeCountdown = (((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF));
		//TODO: sanitize the below values
		this.m_valveNumber = valveNumber;
		this.m_hour = hour;
		this.m_minute =minute;
		this.m_repeatDays = repeatDays;
		this.m_timeCountdown = timeCountdown;
		this.m_masterSwitch = true;
	}


    public ValveOptionsData()
	{
		this(
				MainActivity.mainActivity.getResources().getString(R.string.valve) + staticId,
				(byte)staticId,
				(byte)0,
				(byte)0,
				0,
				new boolean[]{false, false, false, false, false, false, false},
				false
				);
	}

    public ValveOptionsData(String valveName, byte valveNumber, byte hour, byte minute, int timeCountdown, boolean[] repeatDays, boolean masterSwitch) {
        setValveName(valveName);
        setValveNumber(valveNumber);
        setTimeCountdown(timeCountdown);
        setTime(hour, minute);
        setMasterSwitch(masterSwitch);
        setRepeatDay(repeatDays);
    }

	ValveOptionsData(JSONObject obj) throws JSONException
	{
		fromJSON(obj);
	}

	public ValveOptionsData(byte[] bytes, long crc32) throws InvalidObjectException
	{
		fromArduinoBytes(bytes, crc32);
	}

    public void setValveName(String valveName) {
        this.m_valveName = valveName;
    }
	public String getValveName() {
		return m_valveName;
	}

    public void setValveNumber(byte valveNumber) {
        this.m_valveNumber = valveNumber;
    }
	public int getValveNumber() {
		return m_valveNumber;
	}

    public void setTime(byte hour, byte minute)
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
	public int getTimeCountdown() { return m_timeCountdown; }

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
