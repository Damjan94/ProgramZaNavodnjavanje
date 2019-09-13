package com.example.damjan.programzanavodnjavanje.listeners;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.HelpingHand;
import com.example.damjan.programzanavodnjavanje.ActivityHelper.IMainActivity;
import com.example.damjan.programzanavodnjavanje.ActivityHelper.ISetValveData;
import com.example.damjan.programzanavodnjavanje.MainActivity;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.adapters.ConnectibleDevicesAdapter;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;

public class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener
{
    private HelpingHand     m_helper;

    public NavigationItemSelectedListener(@NonNull HelpingHand helper)
    {
        m_helper          = helper;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        boolean                     selectElement   = false;
        RecyclerView.Adapter        adapter         = null;
        MainActivity.AdapterState   state           = MainActivity.AdapterState.NONE;
        switch (item.getItemId())
        {
            case R.id.sync_bluetooth:
            {
                m_helper.sendValves();
                break;
            }
            case R.id.valves_to_send:
            {
                m_helper.setAdapter(new ValveOptionAdapter(m_helper.getValvesToSend(), m_helper), MainActivity.AdapterState.VALVES_TO_SEND);
                break;
            }
            case R.id.valves_on_arduino:
            {
                ArduinoComms.getValves();
                break;
            }
            case R.id.show_errors:
            {
                //adapter = new ErrorAdapter();
                selectElement   = true;
                state  = MainActivity.AdapterState.ERRORS;
                break;
            }
            case R.id.connect_bluetooth:
            {
                selectElement   = true;
                state  = MainActivity.AdapterState.CONNECT_BLUETOOTH;
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                if (defaultAdapter == null)
                {
                    m_helper.showAlertDialog(R.string.no_bluetooth_found);
                    break;
                }
                if (!defaultAdapter.isEnabled())
                {
                    m_helper.showAlertDialog(R.string.enable_bluetooth);
                    break;
                }

                adapter = new ConnectibleDevicesAdapter(defaultAdapter.getBondedDevices());
                break;
            }
        }
        if(selectElement && adapter != null)
        {
            m_helper.setAdapter(adapter, state);
        }
        item.collapseActionView();
        return selectElement;
    }
}
