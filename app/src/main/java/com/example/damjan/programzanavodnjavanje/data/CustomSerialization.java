package com.example.damjan.programzanavodnjavanje.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;

public interface CustomSerialization
{
	JSONObject toJson() throws JSONException;

	void fromJSON(JSONObject jsonIn) throws JSONException;


	byte[] toArduinoBytes();

	void fromArduinoBytes(final byte[] bytes, long crc32) throws InvalidObjectException;
}
