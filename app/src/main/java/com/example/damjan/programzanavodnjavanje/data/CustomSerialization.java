package com.example.damjan.programzanavodnjavanje.data;

import org.json.JSONException;
import org.json.JSONObject;

public interface CustomSerialization
{
	JSONObject toJson() throws JSONException;

	void fromJSON(JSONObject jsonIn) throws JSONException;


	byte[] toArduinoBytes();

	void fromArduinoBytes(final byte[] bytes);
}
