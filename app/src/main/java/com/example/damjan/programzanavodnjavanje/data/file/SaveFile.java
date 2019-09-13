package com.example.damjan.programzanavodnjavanje.data.file;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.R;
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

    private ValveGroups                 m_savedData;
    private ArrayList<Error>            m_errors;
    private ValveGroup                  m_arduinoValves;

    private Activity m_activity;
    private ArrayList<Runnable> m_doneReadingRunnable = new ArrayList<>();

    public SaveFile(@NonNull String safeFileName,@NonNull Activity mainActivity) throws FileNotFoundException
    {
        m_fileOperations            = new PrivateFileOperations(safeFileName, mainActivity, new IFileListener[]{this});
        m_activity                  = mainActivity;
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
        if(!status || fileContents == null)
        {
            m_savedData     = new ValveGroups();
            m_savedData.add(new ValveGroup(m_activity.getString(R.string.new_group)));
            m_errors        = new ArrayList<>();
            m_arduinoValves = new ValveGroup("dummy");
        }
        else {
            try {
                fromJSON(new JSONObject(new String(fileContents)));
            } catch (JSONException e) {
                ConsoleActivity.log(e.toString());
            }
        }
        if(m_savedData.size() == 1)
            m_savedData.selectedGroup = 0;

        for (Runnable run: m_doneReadingRunnable)
        {
            m_activity.runOnUiThread(run);
        }
        m_doneReadingRunnable.clear();
    }

    public void executeWhenDoneReading(Runnable r)
    {
        m_doneReadingRunnable.add(r);
    }

    @Override
    public void doneWriting(boolean status)
    {

    }

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

    public @NonNull ValveGroups getGroups()
    {
        return m_savedData;
    }

    public ValveGroup getValvesToSend()
    {
        //TODO populate the array on the new thread
        ValveGroup valvesToSend = new ValveGroup("valves to send");
        for(ValveGroup grp : m_savedData)
        {
            if(!grp.isEnabled()) continue;
            for(ValveOptionsData data : grp)
            {
                if(!data.isEnabled()) continue;
                valvesToSend.add(data);
            }
        }
        return valvesToSend;
    }

    public void setArduinoValves(ValveOptionsData[] valvesAsync)
    {
        m_arduinoValves.clear();
        m_arduinoValves.addAll(Arrays.asList(valvesAsync));
    }

    public ValveGroup getArduinoValves()
    {
        return m_arduinoValves;
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
