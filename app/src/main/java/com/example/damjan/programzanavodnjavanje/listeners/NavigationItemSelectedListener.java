package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.IMainActivity;
import com.example.damjan.programzanavodnjavanje.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.adapters.ValveGroupAdapter;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveGroups;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;

public class NavigationItemSelectedListener<T extends Context & ISetValveData & IMainActivity> implements NavigationView.OnNavigationItemSelectedListener
{
    private T               m_comm;
    private SaveFile        m_saveFile;
    private MenuItem        m_addButton;
    private SelectedMenu    m_selectedMenu;

    public NavigationItemSelectedListener(@NonNull T comm, @NonNull SaveFile saveFile, MenuItem addButton)
    {
        m_comm          = comm;
        m_saveFile      = saveFile;
        m_addButton     = addButton;
        m_selectedMenu  = SelectedMenu.NONE;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        boolean selectElement           = false;
        RecyclerView.Adapter adapter    = null;
        switch (item.getItemId())
        {
            case R.id.add_valve:
            {
                switch (m_selectedMenu)
                {
                    case SHOW_VALVES:
                    {
                        ValveGroup selectedGroup = m_saveFile.getGroups().get();
                        if(selectedGroup != null)
                            selectedGroup.add(new ValveOptionsData(m_comm.getResources().getString(R.string.valve)));
                        else
                            m_comm.showAlertDialog(R.string.show_valves_add_error);
                        break;
                    }
                    case SHOW_VALVE_GROUPS:
                    {
                        m_saveFile.getGroups().add(new ValveGroup("New Group"));
                        break;
                    }
                }
                //TODO notify recycler view that we have added another item
                break;
            }
            case R.id.sync_bluetooth:
            {
                m_comm.sendValves();
                break;
            }
            case R.id.show_valve_groups:
            {
                adapter = new ValveGroupAdapter(m_saveFile.getGroups(), (Activity) m_comm, m_saveFile);
                selectElement   = true;
                m_selectedMenu  = SelectedMenu.SHOW_VALVE_GROUPS;
                setAddButtonText(R.string.add_group);
                break;
            }
            case R.id.show_errors:
            {
                //adapter = new ErrorAdapter();
                selectElement   = true;
                m_selectedMenu  = SelectedMenu.SHOW_ERRORS;
                break;
            }
            case R.id.show_valves:
            {
                ValveGroup selectedGroup = m_saveFile.getGroups().get();
                if(selectedGroup != null)
                {
                    adapter = new ValveOptionAdapter(m_saveFile.getGroups().get(), (Activity) m_comm, m_saveFile);
                    selectElement   = true;
                    m_selectedMenu  = SelectedMenu.SHOW_VALVES;
                    setAddButtonText(R.string.add_valve);
                }
                else
                    m_comm.showAlertDialog(R.string.show_valves_error);
                break;
            }
            case R.id.connect_bluetooth:
            {
                //adapter = new ConnectableDevicesAdapter();
                selectElement   = true;
                m_selectedMenu  = SelectedMenu.CONNECT_BLUETOOTH;
                /*
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                if (defaultAdapter == null)
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder();
                    String msg = Resources.getSystem().getString(R.string.no_bluetooth_found);
                    alertDialog.setMessage(msg);
                    alertDialog.show();
                    break;
                }
                if (!defaultAdapter.isEnabled())
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    String msg = Resources.getSystem().getString(R.string.enable_bluetooth);
                    alertDialog.setMessage(msg);
                    alertDialog.show();
                    break;
                }

                BluetoothDevice device = defaultAdapter.getBondedDevices().iterator().next();
                ArduinoComms.connect(device);
                */
                break;
            }
        }
        if(selectElement && adapter != null)
            m_comm.setAdapter(adapter);
        item.collapseActionView();
        return selectElement;
    }
    private enum SelectedMenu
    {
        NONE,
        SHOW_VALVE_GROUPS,
        SHOW_ERRORS,
        SHOW_VALVES,
        CONNECT_BLUETOOTH
    }

    private void setAddButtonText(int textID)
    {
        m_addButton.setTitle(textID);
    }
}
