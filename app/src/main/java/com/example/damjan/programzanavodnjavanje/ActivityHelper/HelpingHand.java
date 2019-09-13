package com.example.damjan.programzanavodnjavanje.ActivityHelper;
//TODO think of a better name for the class

import android.app.DialogFragment;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.data.Error;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveGroups;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.IBluetoothComms;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;

import java.io.FileNotFoundException;
import java.util.Calendar;

/**
 * This class is used to declutter the MainActivity.
 * Anything that isn't Activity related should go here
 **/
public class HelpingHand implements IMainActivity, ISetValveData, ISetValveGroupData, IBluetoothComms
{

    private MainActivity    m_mainActivity;
    private SaveFile        m_saveFile;

    public HelpingHand(MainActivity activity, String saveFileName)
    {
        m_mainActivity  = activity;
        try
        {
            m_saveFile = new SaveFile(saveFileName, m_mainActivity);
        } catch (FileNotFoundException e)
        {
            m_saveFile = null;
            showToast(Resources.getSystem().getString(R.string.cant_open_file) + " : " + saveFileName, Toast.LENGTH_SHORT);
            ConsoleActivity.log(e.toString());
        }
    }

    public int getGroupsSize()
    {
        return m_saveFile.getGroups().size();
    }

    public SaveFile getSaveFile()
    {

    }


//------------------ValveGroup stuff--------------------------
    @Override
    public void addGroup(ValveGroup group)
    {
        if(group == null)
            return;
        m_saveFile.getGroups().add(group);
    }
    @Override
    public void removeGroup(int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        m_saveFile.getGroups().remove(viewHolderPosition);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.REMOVE_ITEM);
        if(m_saveFile.getGroups().size() == 1)
            showMainMenu();
    }

    @Override
    public void setGroupName(String name, int viewHolderPosition)
    {
        ValveGroup group = m_saveFile.getGroups().get(viewHolderPosition);
        if(group == null)
            return;
        group.setGroupName(name);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.CHANGE_ITEM);
    }

    @Override
    public void setActiveGroupName(String name)
    {
        if(m_adapterState != MainActivity.AdapterState.VALVES && m_saveFile.getGroups().selectedGroup < 0)
            return;
        m_saveFile.getGroups().get().setGroupName(name);
        m_selectedGroupName.setText(name);
    }

    @Override
    public void setGroupPercent(int newPercent, int adapterPosition)
    {
        m_saveFile.getGroups().get(adapterPosition).setPercent(Math.min(newPercent, 100));
        adapterItemChanged(adapterPosition);
    }

    @Override
    public void groupSelected(int selectedGroupPos)
    {
        m_saveFile.getGroups().selectedGroup = selectedGroupPos;
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
            return;
        m_selectedGroupName.setText(group.getGroupName());
        this.setAdapter(new ValveOptionAdapter<>(group, this, m_saveFile), MainActivity.AdapterState.VALVES);
    }

//------------------------Valve stuff----------------------
    public void addValve(ValveOptionsData item)
    {
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
        {
            showAlertDialog(R.string.cant_add_valve_error);
            return;
        }
        group.add(item);
        notifyItemChanged(group.size() - 1, MainActivity.Job.ADD_ITEM);
    }

    @Override
    public void removeValve(int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
            return;
        group.remove(viewHolderPosition);
        //ValveGroup.groups.get(0).removeValveOptionData(viewHolderPosition);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.REMOVE_ITEM);
    }

    @Override
    public void setValveName(String name, int viewHolderPosition)
    {
        if(m_saveFile.getGroups().selectedGroup == ValveGroups.INVALID_SELECTED_GROUP)
            return;

        ValveOptionsData data = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setValveName(name);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.CHANGE_ITEM);
    }

    @Override
    public void setTime(byte hour, byte minute, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setTime(hour, minute);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.CHANGE_ITEM);
    }

    @Override
    public void setTimeCountdown(int timeCountdown, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setTimeCountdown(timeCountdown);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.CHANGE_ITEM);
    }

    @Override
    public void setValveNumber(byte num, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setValveNumber(num);
        notifyItemChanged(viewHolderPosition, MainActivity.Job.CHANGE_ITEM);
    }

    public void setValveMasterSwitch(int valveIndex, boolean value)
    {
        ValveGroup group = m_saveFile.getGroups().get();
        if(!validateValveIndex(group, valveIndex))
            return;
        group.get(valveIndex).setMasterSwitch(value);
    }

    public void setDayCheckBox(int valveIndex, boolean value, int day)
    {
        ValveGroup group = m_saveFile.getGroups().get();
        if(!validateValveIndex(group, valveIndex))
            return;
        group.get(valveIndex).setRepeatDay(value, day);
    }

    @Override
    public void sendValves()
    {
        ArduinoComms.sendValves(m_saveFile.getValvesToSend());
    }

    @Override
    public void showTemperature()
    {

    }

//--------------------Activity stuff----------------------------
    @Override
    public void setAdapter(RecyclerView.Adapter adapter, MainActivity.AdapterState state)
    {
        m_RecyclerView.setAdapter(adapter);
        m_adapterState = state;
        m_drawerLayout.closeDrawer(Gravity.START);
    }

    @Override
    public void adapterItemChanged(int pos)
    {
        RecyclerView.Adapter adapter = m_RecyclerView.getAdapter();
        if(adapter != null)
            adapter.notifyItemChanged(pos);
    }

    //----------------Bluetooth stuff-----------------

    @Override
    public void connected()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            boolean connected = true;
            m_connectBluetooth.setEnabled(!connected);
            m_syncBluetooth.setEnabled(connected);
            m_showErrors.setEnabled(connected);
            m_disconnect.setEnabled(connected);
            m_valvesOnArduino.setEnabled(connected);
            m_getTempButton.setEnabled(connected);

            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        });
    }

    @Override
    public void connectionFailed()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            boolean connected = false;
            m_connectBluetooth.setEnabled(!connected);
            m_syncBluetooth.setEnabled(connected);
            m_showErrors.setEnabled(connected);
            m_disconnect.setEnabled(connected);
            m_valvesOnArduino.setEnabled(connected);
            m_getTempButton.setEnabled(connected);
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION*3, VibrationEffect.DEFAULT_AMPLITUDE));
        });
    }

    @Override
    public void disconnected()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {

            boolean connected = false;
            m_connectBluetooth.setEnabled(!connected);
            m_syncBluetooth.setEnabled(connected);
            m_showErrors.setEnabled(connected);
            m_disconnect.setEnabled(connected);
            m_valvesOnArduino.setEnabled(connected);
            m_getTempButton.setEnabled(connected);
            Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void setTemperature(String temperature)
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
                m_temperatureText.setText(temperature));
    }

    @Override
    public void setTime(Calendar timeAsync)
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            m_dateTimeText.setText(formatter.format(timeAsync.getTime()));
        });
    }

    @Override
    public void setValves(ValveOptionsData[] valvesAsync)
    {
        m_saveFile.setArduinoValves(valvesAsync);
        m_mainActivity.runOnUiThread(() ->
        {
            this.setAdapter(new ValveOptionAdapter(m_saveFile.getArduinoValves(), null), MainActivity.AdapterState.VALVES_ON_ARDUINO);
        });
    }
    @Override
    public void setErrors(Error[] errors) { }




    //-----------------Dialogs-------------------------

    @Override
    public void showToast(String text, int length)
    {
        Toast.makeText(m_mainActivity, text, length).show();
    }

    @Override
    public void showToast(int resID, int length)
    {
        String resource;
        try
        {
            resource = Resources.getSystem().getString(resID);
        }
        catch (Resources.NotFoundException e)
        {
            resource = Resources.getSystem().getString(R.string.resource_not_found);
        }
        showToast(resource, length);
    }

    @Override
    public void showAlertDialog(int messageID)
    {
        Dialogs.MyAlertDialog alert = new Dialogs.MyAlertDialog();
        Bundle args = new Bundle();
        args.putInt(Dialogs.MyAlertDialog.MESSAGE_ID_KEY, messageID);
        alert.setArguments(args);
        alert.show(m_mainActivity.getFragmentManager(), "alert");
    }

    public void showAlertDialog(String message, @StringRes int positiveID, @Nullable AlertDialog.OnClickListener positiveRun, @StringRes int negativeID, @Nullable Runnable negativeRun)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(m_mainActivity);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(positiveID, positiveRun);
        alertDialog.setNegativeButton(R.string.negative_response, null);
        alertDialog.show();
    }

    public void showTimePicker(int position)
    {
        DialogFragment timePicker = new Dialogs.TimePicker();
        Bundle bundle = new Bundle();
        bundle.putInt(Dialogs.POSITION, position);
        timePicker.setArguments(bundle);
        timePicker.show(m_mainActivity.getFragmentManager(), "timePicker");
    }

    public void showMinutePicker(int position)
    {
        DialogFragment minutePicker = new Dialogs.MinutePicker();
        Bundle bundle = new Bundle();
        bundle.putInt(Dialogs.POSITION, position);
        minutePicker.setArguments(bundle);
        minutePicker.show(m_mainActivity.getFragmentManager(), "minutePicker");
    }

    public void showNamePicker(int position)
    {
        DialogFragment namePicker = new Dialogs.NamePicker();
        Bundle bundle = new Bundle();
        bundle.putInt(Dialogs.POSITION, position);
        namePicker.setArguments(bundle);
        namePicker.show(m_mainActivity.getFragmentManager(), "namePicker");
    }

    public void showNumberPicker(int position)
    {
        DialogFragment numberPicker = new Dialogs.NumberPicker();
        Bundle bundle = new Bundle();
        bundle.putInt(Dialogs.POSITION, position);
        bundle.putInt(Dialogs.FUNCTION, Dialogs.FUNCTION_UPDATE_NUMBER);
        numberPicker.setArguments(bundle);
        numberPicker.show(m_mainActivity.getFragmentManager(), "numberPicker");
    }

    //-----------------Helper functions----------------

    private ValveOptionsData getValveOptionData(int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return null;
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
            return null;
        return group.get(viewHolderPosition);
    }

    private boolean validateValveIndex(ValveGroup group, int valveIndex)
    {
        return group != null &&
                valveIndex >= 0 &&
                valveIndex <= group.size() -1 ;
    }
}
