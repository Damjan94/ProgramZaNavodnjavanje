package com.example.damjan.programzanavodnjavanje.data;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ValveGroups extends ArrayList<ValveGroup> implements JsonSerializable
{
    private final static String VALVE_GROUP_ARRAY_STRING    = "valve group arrays";

    public final static int INVALID_SELECTED_GROUP          = -1;
    //private ArrayList<ValveGroup> m_groupArray;
    public int selectedGroup = INVALID_SELECTED_GROUP;

    public ValveGroups()
    {
    }

    public ValveGroups(JSONObject valveGroups) throws JSONException
    {
        fromJSON(valveGroups);
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (ValveGroup grp : this)
        {
            arr.put(grp.toJson());
        }
        obj.put(VALVE_GROUP_ARRAY_STRING, arr);
        return obj;
    }

    @Override
    public void fromJSON(JSONObject jsonIn) throws JSONException
    {
        JSONArray valveGroups = jsonIn.getJSONArray(VALVE_GROUP_ARRAY_STRING);
        final int length = valveGroups.length();
        for(int i = 0; i < length; i++)
        {
            this.add(new ValveGroup(valveGroups.getJSONObject(i)));
        }
    }
/*
    public ArrayList<ValveGroup> getGroups(){ return m_groupArray; }

    public ValveGroup get(int index)
    {
        return m_groupArray.get(index);
    }
*/
    public @Nullable ValveGroup get()
    {
        return (selectedGroup >=0 && selectedGroup < this.size())? this.get(selectedGroup) : null;
    }

}
