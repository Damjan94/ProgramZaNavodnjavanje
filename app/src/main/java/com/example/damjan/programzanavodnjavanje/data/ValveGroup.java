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
	private ArrayList<ValveOptionsData> m_valveOptionDataList = new ArrayList<>();

	private String m_groupName = "Group0";
	private int m_percentage = 100;

	private static final String GROUP_NAME = "GroupName";
	private static final String PERCENT = "Percent";

	public ValveGroup()
	{

	}

	public ValveGroup(JSONObject obj) throws JSONException
	{
		fromJSON(obj);
	}

	public ArrayList<ValveOptionsData> getValveOptionDataCollection()
	{
		return m_valveOptionDataList;
	}
	public void setValveOptionDataCollection(ArrayList<ValveOptionsData> dataList)
	{
		m_valveOptionDataList = dataList;
	}

	public ValveOptionsData getValveOptionData(int pos)
	{
		return m_valveOptionDataList.get(pos);
	}

	public void addValveOptionData(ValveOptionsData data)
	{
		m_valveOptionDataList.add(data);
	}

	public void removeValveOptionData(int pos)
	{
		m_valveOptionDataList.remove(pos);
	}

	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put(GROUP_NAME, m_groupName);
		JSONArray arr = new JSONArray();
		for(ValveOptionsData data : m_valveOptionDataList)
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
		m_valveOptionDataList.clear();
		JSONArray arr = jsonIn.getJSONArray(m_groupName);
		for(int i = 0; i < arr.length(); i++)
		{
			m_valveOptionDataList.add(new ValveOptionsData((JSONObject) arr.get(i)));
		}
	}

	@Override
	public byte[] toArduinoBytes()
	{
		int bufferSize = m_valveOptionDataList.size() * ValveOptionsData.NETWORK_SIZE;
		byte[] bytes = new byte[bufferSize];

		for(int i = 0; i < bufferSize; i += ValveOptionsData.NETWORK_SIZE)
		{
			System.arraycopy((m_valveOptionDataList.get(i/ValveOptionsData.NETWORK_SIZE).toArduinoBytes()), 0,
					bytes, i, ValveOptionsData.NETWORK_SIZE);
		}

		return bytes;
	}

	@Override
	final public void fromArduinoBytes(byte[] bytes, long crc32)
	{
		throw new UnsupportedOperationException();
	}
}
