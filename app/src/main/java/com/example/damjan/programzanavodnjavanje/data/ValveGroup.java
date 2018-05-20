package com.example.damjan.programzanavodnjavanje.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ValveGroup implements CustomSerialization
{

	public static ArrayList<ValveGroup> groups = new ArrayList<>();
	static
	{
		groups.add(new ValveGroup());
	}
	private ArrayList<ValveOptionsData> m_valveGroup = new ArrayList<>();
	private String m_groupName = "Group0";
	private static final String GROUP_NAME = "GroupName";

	public ValveGroup()
	{

	}

	public ValveGroup(JSONObject obj) throws JSONException
	{
		fromJSON(obj);
	}

	public ArrayList<ValveOptionsData> getValveOptionDataCollection()
	{
		return m_valveGroup;
	}
	public void setValveOptionDataCollection(ArrayList<ValveOptionsData> dataList)
	{
		m_valveGroup = dataList;
	}

	public ValveOptionsData getValveOptionData(int pos)
	{
		return m_valveGroup.get(pos);
	}

	public void addValveOptionData(ValveOptionsData data)
	{
		m_valveGroup.add(data);
	}

	public void removeValveOptionData(int pos)
	{
		m_valveGroup.remove(pos);
	}

	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put(GROUP_NAME, m_groupName);
		JSONArray arr = new JSONArray();
		for(ValveOptionsData data : m_valveGroup)
		{
			arr.put(data.toJson());
		}
		obj.put(m_groupName, arr);
		return obj;
	}

	@Override
	final public void fromJSON(JSONObject jsonIn) throws JSONException
	{
		m_groupName = (String)jsonIn.get(GROUP_NAME);
		m_valveGroup.clear();
		JSONArray arr = jsonIn.getJSONArray(m_groupName);
		for(int i = 0; i < arr.length(); i++)
		{
			m_valveGroup.add(new ValveOptionsData((JSONObject) arr.get(i)));
		}
	}

	@Override
	public byte[] toArduinoBytes()
	{
		int bufferSize = m_valveGroup.size() * ValveOptionsData.VALVE_DATA_NETWORK_SIZE;
		byte[] bytes = new byte[bufferSize];

		for(int i = 0; i < bufferSize; i += ValveOptionsData.VALVE_DATA_NETWORK_SIZE)
		{
			System.arraycopy((m_valveGroup.get(i/ValveOptionsData.VALVE_DATA_NETWORK_SIZE).toArduinoBytes()), 0,
					bytes, i, ValveOptionsData.VALVE_DATA_NETWORK_SIZE);
		}

		return bytes;
	}

	@Override
	final public void fromArduinoBytes(byte[] bytes)
	{
		m_valveGroup.clear();
		byte[] dest = new byte[ValveOptionsData.VALVE_DATA_NETWORK_SIZE];
		for(int i = 0; i < bytes.length; i+=ValveOptionsData.VALVE_DATA_NETWORK_SIZE)
		{
			System.arraycopy(bytes, i, dest, 0, ValveOptionsData.VALVE_DATA_NETWORK_SIZE);
			m_valveGroup.add(new ValveOptionsData(dest));
		}
	}
}
