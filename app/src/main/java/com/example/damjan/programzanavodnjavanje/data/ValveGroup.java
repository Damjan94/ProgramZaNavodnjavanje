package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.adapters.ValveGroupAdapter;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ValveGroup extends ArrayList<ValveOptionsData> implements JsonSerializable
{
    private static final String GROUP_NAME  = "GroupName";
    private static final String PERCENT     = "Percent";
    private static final String STATUS      = "Is on";

    private String  m_groupName;
    private int m_percent;
    private boolean m_isOn;

    public ValveGroup(String name, int percentage)
    {
        this(name, percentage, new ArrayList<>());
    }
    public ValveGroup(String name)
    {
        this(name, 100);
    }

    public ValveGroup(JSONObject obj) throws JSONException
    {
        this("");
        fromJSON(obj);
    }

    public ValveGroup(String name, ValveOptionsData[] valves)
    {
        this(name, 100, new ArrayList<>(Arrays.asList(valves)));
    }

    public ValveGroup(String name, int percentage, ArrayList<ValveOptionsData> valves)
    {
        super(valves);
        m_groupName             = name;
        m_percent               = percentage;
        m_isOn                  = true;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(GROUP_NAME, m_groupName);
        obj.put(PERCENT, m_percent);
        obj.put(STATUS, m_isOn);
        JSONArray arr = new JSONArray();
        for (ValveOptionsData data : this)
        {
            arr.put(data.toJson());
        }
        obj.put(m_groupName, arr);
        return obj;
    }

    @Override
    final public void fromJSON(JSONObject jsonIn) throws JSONException
    {
        m_groupName     = jsonIn.getString(GROUP_NAME);
        m_percent       = jsonIn.getInt(PERCENT);
        m_isOn          = jsonIn.getBoolean(STATUS);
        this.clear();
        JSONArray arr = jsonIn.getJSONArray(m_groupName);
        for (int i = 0; i < arr.length(); i++)
        {
            this.add(new ValveOptionsData((JSONObject) arr.get(i)));
        }
    }

    public String  getGroupName()  { return m_groupName; }
    public int     getPercent() { return m_percent;}
    public boolean isEnabled()     { return m_isOn; }

    public void setGroupName(String newName) { m_groupName = newName; }
    public void setPercent(int newPercent) { m_percent = newPercent; }
    public void setStatus(boolean newStatus) { m_isOn = newStatus ; }
}
