package com.example.damjan.programzanavodnjavanje.data.file;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.data.Error;
import com.example.damjan.programzanavodnjavanje.data.JsonSerializable;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveGroups;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.IBluetoothComms;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class SaveFile implements JsonSerializable, IFileListener, IBluetoothComms
{
    private static final int    SAVE_FILE_VERSION = 0;

    private static final String SAVE_FILE_VERSION_STRING = "Save file version";
    private static final String USER_VALVES_STRING       = "User valves";
    private static final String ARDUINO_ERRORS_STRING    = "Arduino errors";
    private static final String ARDUINO_VALVES_STRING    = "Arduino valves";


    private final PrivateFileOperations m_fileOperations;

    private boolean                     m_wasLastWriteSuccessful;
    private boolean                     m_wasLastReadSuccessful;
    private ValveGroups                 m_savedData;
    private ArrayList<Error>            m_errors;
    private ValveGroup                  m_arduinoValves;

    public SaveFile(@NonNull String safeFileName,@NonNull Context mainActivity) throws FileNotFoundException
    {
        m_fileOperations            = new PrivateFileOperations(safeFileName, mainActivity, new IFileListener[]{this});
        m_wasLastWriteSuccessful    = true;
        m_wasLastReadSuccessful     = true;
        m_savedData                 = null;
        m_errors                    = null;
        m_arduinoValves             = null;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject data = new JSONObject();
        data.put(SAVE_FILE_VERSION_STRING, SAVE_FILE_VERSION);

        data.put(USER_VALVES_STRING, m_savedData.toJson());

        JSONArray errors = new JSONArray();
        for (Error e : m_errors)
        {
            errors.put(e.toJson());
        }

        data.put(ARDUINO_ERRORS_STRING, errors);

        data.put(ARDUINO_VALVES_STRING, m_arduinoValves.toJson());
        return data;
    }

    @Override
    public void fromJSON(JSONObject jsonIn) throws JSONException
    {
        int readVersion = jsonIn.getInt(SAVE_FILE_VERSION_STRING);
        if(readVersion != SAVE_FILE_VERSION)
            ConsoleActivity.log("Save File version mismatch!\tGot " +  readVersion + "\tOur version is " + SAVE_FILE_VERSION);

        m_savedData = new ValveGroups(jsonIn.getJSONObject(USER_VALVES_STRING));

        JSONArray errors = jsonIn.getJSONArray(ARDUINO_ERRORS_STRING);
        if(m_errors == null) m_errors = new ArrayList<>();
        for(int i = 0; i < errors.length(); i++)
        {
            m_errors.add(new Error(errors.getJSONObject(i)));//always add new errors, never delete them
        }

        m_arduinoValves = new ValveGroup(jsonIn.getJSONObject(ARDUINO_VALVES_STRING));
    }

    @Override
    public void doneReading(@Nullable byte[] fileContents, boolean status)
    {
        m_wasLastReadSuccessful = status;
        if(!status || fileContents == null)
        {
            m_savedData     = new ValveGroups();
            m_errors        = new ArrayList<>();
            m_arduinoValves = new ValveGroup("dummy");
            return;
        }
        try
        {
            fromJSON(new JSONObject(new String(fileContents)));
        } catch (JSONException e)
        {
            m_wasLastReadSuccessful = false;
        }
    }

    @Override
    public void doneWriting(boolean status)
    {
        m_wasLastWriteSuccessful = status;
    }

    public boolean wasLastWriteSuccessful(){return m_wasLastWriteSuccessful;}
    public boolean wasLastReadSuccessful() {return m_wasLastReadSuccessful;}

    public void saveData()
    {
        try
        {
            m_fileOperations.writeAsync(this.toJson().toString().getBytes());
        } catch (JSONException e)
        {
            ConsoleActivity.log(e.toString());
        }
    }
    public void read()
    {

        m_savedData = null;
        m_fileOperations.readAsync();
    }

    public ValveGroups getGroups()
    {
        return m_savedData;
    }

    @Override
    public void connected()
    {

    }

    @Override
    public void connectionFailed()
    {

    }

    @Override
    public void disconnected()
    {

    }

    @Override
    public void setTemperature(String temperature)
    {

    }

    @Override
    public void setTime(Calendar time)
    {

    }

    @Override
    public void setValves(ValveOptionsData[] valves)
    {
        m_arduinoValves = new ValveGroup(ARDUINO_VALVES_STRING, valves);
    }

    @Override
    public void setErrors(Error[] errors)
    {
        m_errors = new ArrayList<>(Arrays.asList(errors));
    }
}
