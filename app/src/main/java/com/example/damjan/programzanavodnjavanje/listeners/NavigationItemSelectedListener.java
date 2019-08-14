package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.damjan.programzanavodnjavanje.IMainActivity;
import com.example.damjan.programzanavodnjavanje.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.adapters.ValveGroupAdapter;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;

public class NavigationItemSelectedListener<T extends AppCompatActivity & ISetValveData & IMainActivity>
        implements NavigationView.OnNavigationItemSelectedListener
{
    private T               m_comm;
    private SaveFile        m_saveFile;
    private SelectedMenu    m_selectedMenu;

    public NavigationItemSelectedListener(@NonNull T comm, @NonNull SaveFile saveFile)
    {
        m_comm          = comm;
        m_saveFile      = saveFile;
        m_selectedMenu  = SelectedMenu.NONE;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        boolean selectElement           = false;
        RecyclerView.Adapter adapter    = null;
        switch (item.getItemId())
        {
            case R.id.sync_bluetooth:
            {
                m_comm.sendValves();
                break;
            }
            case R.id.show_valve_groups:
            {
                adapter = new ValveGroupAdapter<>(m_saveFile.getGroups(), m_comm, m_saveFile);
                adapter.setHasStableIds(false);
                selectElement   = true;
                m_selectedMenu  = SelectedMenu.SHOW_VALVE_GROUPS;
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
        {
            m_comm.setAdapter(adapter);
        }
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
}
