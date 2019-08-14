package com.example.damjan.programzanavodnjavanje;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.data.Error;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveGroups;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.data.bluetooth.IBluetoothComms;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.listeners.NavigationItemSelectedListener;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;

import java.io.FileNotFoundException;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements ISetValveData, IBluetoothComms, IMainActivity
{
    private final static String SAVE_FILE_NAME = "my_file.json";

    private SaveFile        m_saveFile;

    private RecyclerView    m_RecyclerView;

    private Menu            m_drawerMenu;
    private ImageButton     m_getTempButton;
    private MenuItem        m_connectBluetooth;
    private MenuItem        m_syncBluetooth;

    private TextView        m_temperatureText;
    private TextView        m_dateTimeText;

    private TextView        m_selectedGroupName;

    private static int m_backPresses = 0;


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.add_valve:
            {
                if(m_saveFile.getGroups().selectedGroup != ValveGroups.INVALID_SELECTED_GROUP)
                {
                        ValveGroup selectedGroup = m_saveFile.getGroups().get();
                        if(selectedGroup != null)
                        {
                            selectedGroup.add(new ValveOptionsData(getString(R.string.valve)));
                            m_RecyclerView.getAdapter().notifyItemInserted(selectedGroup.size() -1);
                        }
                        else
                            showAlertDialog(R.string.cant_add_valve_error);
                }else
                {
                    m_saveFile.getGroups().add(new ValveGroup("New Group"));
                    m_RecyclerView.getAdapter().notifyItemInserted(m_saveFile.getGroups().size() -1);
                }
                //TODO notify recycler view that we have added another item

                break;
            }
            case R.id.disconnect:
            {
                ArduinoComms.disconnect();
                break;
            }
            case R.id.valves_to_send:
            {
                //TODO change recycler view to display the valves that are going to be sent
                throw new UnsupportedOperationException();
            }
            case R.id.valves_on_arduino:
            {
                //TODO change recycler view to display the valves that are currently on the arduino
                throw new UnsupportedOperationException();
            }
            case R.id.console_mode_button:
            {
                Intent i = new Intent(this, ConsoleActivity.class);
                startActivity(i);
                break;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    void setUpRecyclerView()
    {
        m_RecyclerView = findViewById(R.id.mainFragmentHolder);
        m_RecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        linearLayout.setOrientation(LinearLayoutManager.VERTICAL);
        m_RecyclerView.setLayoutManager(linearLayout);

        DividerItemDecoration decoration = new DividerItemDecoration(this, linearLayout.getOrientation());
        m_RecyclerView.addItemDecoration(decoration);
        ((DefaultItemAnimator) m_RecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public void connected()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            //syncBluetooth.setEnabled(true);
            m_connectBluetooth.setEnabled(true);
            m_syncBluetooth.setEnabled(false);
            m_getTempButton.setEnabled(true);
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void connectionFailed()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            m_connectBluetooth.setEnabled(true);
            m_syncBluetooth.setEnabled(false);
            m_getTempButton.setEnabled(false);
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void disconnected()
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            m_connectBluetooth.setEnabled(true);
            m_syncBluetooth.setEnabled(false);
            m_getTempButton.setEnabled(false);
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
                m_dateTimeText.setText(timeAsync.toString()));
    }

    @Override
    public void setValves(ValveOptionsData[] valvesAsync) { }
    @Override
    public void setErrors(Error[] errors) { }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            m_saveFile = new SaveFile(SAVE_FILE_NAME, this);
        } catch (FileNotFoundException e)
        {
            m_saveFile = null;
            Toast.makeText(this, Resources.getSystem().getString(R.string.cant_open_file) + " : " + SAVE_FILE_NAME, Toast.LENGTH_SHORT).show();
            ConsoleActivity.log(e.toString());
        }

        setContentView(R.layout.activity_main);

        m_selectedGroupName     = findViewById(R.id.selected_view_group_name);

        NavigationView navView  = findViewById(R.id.nav_view);

        m_drawerMenu            = navView.getMenu();
        m_connectBluetooth      = m_drawerMenu.findItem(R.id.connect_bluetooth);
        m_syncBluetooth         = m_drawerMenu.findItem(R.id.sync_bluetooth);

        navView.setNavigationItemSelectedListener(new NavigationItemSelectedListener<>(this, m_saveFile));
        View headerView         = navView.getHeaderView(0);
        m_getTempButton         = headerView.findViewById(R.id.getTempButton);
        m_getTempButton.setOnClickListener((View V) ->
        {
            ArduinoComms.getTempFloat();
            ArduinoComms.getTime();
        });
        m_getTempButton.setEnabled(false);

        m_temperatureText   = headerView.findViewById(R.id.temperatureValue);
        m_dateTimeText      = headerView.findViewById(R.id.timeValue);


        setUpRecyclerView();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //read saved data
        m_saveFile.read();
        //TODO notify us that we have read the data, but make sure not to do it while we are paused
        ArduinoComms.registerListener(this);
        ArduinoComms.registerListener(m_saveFile);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(m_saveFile.wasLastReadSuccessful())
            m_drawerMenu.performIdentifierAction(R.id.show_valve_groups, 0);
        else
        {
            android.os.Handler mHandler = new Handler();
            mHandler.postAtTime(() ->
                    {
                        if(m_saveFile.wasLastReadSuccessful())
                            m_drawerMenu.performIdentifierAction(R.id.show_valve_groups, 0);
                    }, android.os.SystemClock.uptimeMillis()+1000);//delay selecting the view for 1 sec(hopefully it loads by then)

        }
        //get bluetooth stuff done
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //release bluetooth stuff
        //ArduinoComms.disconnect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //save stuff
        m_saveFile.saveData();
        ArduinoComms.unregisterListener(this);
        ArduinoComms.unregisterListener(m_saveFile);
    }

    void notifyItemChanged(final int pos, final Job job)
    {
        android.os.Handler mHandler = getWindow().getDecorView().getHandler();
        mHandler.post(() ->
        {
            //change adapter contents
            switch (job)
            {
                case ADD_ITEM:
                {
                    m_RecyclerView.getAdapter().notifyItemInserted(pos);
                    break;
                }
                case REMOVE_ITEM:
                {
                    m_RecyclerView.getAdapter().notifyItemRemoved(pos);
                    break;
                }
                case CHANGE_ITEM:
                {
                    m_RecyclerView.getAdapter().notifyItemChanged(pos);
                    break;
                }
            }
        });
    }

    private ValveOptionsData getValveOptionData(int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return null;
        ValveGroup group        = m_saveFile.getGroups().get();
        if(group == null)
            return null;
        ValveOptionsData data = group.get(viewHolderPosition);
        if (data == null)
            return null;
        return data;
    }

    @Override
    public void addItem(ValveOptionsData item)
    {
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
        {
            displayToast(getString(R.string.cant_add_valve_error), Toast.LENGTH_SHORT);
            return;
        }
        group.add(item);
        notifyItemChanged(group.size() - 1, Job.ADD_ITEM);
    }

    @Override
    public void removeItem(int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
            return;
        group.remove(viewHolderPosition);
        //ValveGroup.groups.get(0).removeValveOptionData(viewHolderPosition);
        notifyItemChanged(viewHolderPosition, Job.REMOVE_ITEM);
    }

    @Override
    public void setTime(byte hour, byte minute, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setTime(hour, minute);
        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }

    @Override
    public void setTimeCountdown(int timeCountdown, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setTimeCountdown(timeCountdown);
        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }

    @Override
    public void setValveNumber(byte num, int viewHolderPosition)
    {
        ValveOptionsData data   = getValveOptionData(viewHolderPosition);
        if (data == null)
            return;

        data.setValveNumber(num);
        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }

    @Override
    public void setValveName(String name, int viewHolderPosition)
    {
        if(m_saveFile.getGroups().selectedGroup != ValveGroups.INVALID_SELECTED_GROUP)
        {
            ValveOptionsData data = getValveOptionData(viewHolderPosition);
            if (data == null)
                return;

            data.setValveName(name);
        }
        else
        {
            ValveGroup group = m_saveFile.getGroups().get(viewHolderPosition);
            if(group == null)
                return;
            group.setGroupName(name);
        }
        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }
/*
    @Override
    public void setValveDayOn(boolean[] days, int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data =m_valveGroups.get().getValveOptionData(viewHolderPosition);
        if (data == null)
            return;
        data.setRepeatDay(days);

        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }

    @Override
    public void setValveDayOn(boolean value, int day, int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = m_valveGroups.get().getValveOptionData(viewHolderPosition);
        if (data == null)
            return;
        data.setRepeatDay(value, day);

        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);
    }

    @Override
    public void setMasterSwitch(boolean value, int viewHolderPosition)
    {
        if (viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = m_valveGroups.get().getValveOptionData(viewHolderPosition);
        if (data == null)
            return;
        data.setMasterSwitch(value);

        notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM);//TODO doesn't mean that the recycler view is displaying this list currently
    }
*/
    @Override
    public void displayToast(String text, int length)
    {
        Toast.makeText(this, text, length).show();
    }

    @Override
    public void setGroupPercent(int newPercent, int adapterPosition)
    {
        m_saveFile.getGroups().get(adapterPosition).setPercent(Math.min(newPercent, 100));
        adapterItemChanged(adapterPosition);
    }

    @Override
    public void sendValves()
    {

    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter)
    {
        m_RecyclerView.setAdapter(adapter);
    }

    @Override
    public void adapterItemChanged(int pos)
    {
        RecyclerView.Adapter adapter = m_RecyclerView.getAdapter();
        if(adapter != null)
            adapter.notifyItemChanged(pos);
    }

    @Override
    public void groupSelected(int selectedGroupPos)
    {
        m_saveFile.getGroups().selectedGroup = selectedGroupPos;
        ValveGroup group = m_saveFile.getGroups().get();
        if(group == null)
            return;
        m_selectedGroupName.setText(group.getGroupName());
        m_drawerMenu.performIdentifierAction(R.id.show_valves, 0);
    }

    @Override
    public void showTemperature()
    {

    }

    @Override
    public void showAlertDialog(int messageID)
    {
        Dialogs.MyAlertDialog alert = new Dialogs.MyAlertDialog();
        Bundle args = new Bundle();
        args.putInt(Dialogs.MyAlertDialog.MESSAGE_ID_KEY, messageID);
        alert.setArguments(args);
        alert.show(getFragmentManager(), "alert");
    }

    @Override
    public void doOnUiThread(Runnable r)
    {
        runOnUiThread(r);
    }
    @Override
    public void onBackPressed()
    {
        if(m_saveFile.getGroups().selectedGroup >=0)
        {
            m_saveFile.getGroups().selectedGroup = ValveGroups.INVALID_SELECTED_GROUP;
            m_selectedGroupName.setText("");
            m_drawerMenu.performIdentifierAction(R.id.show_valve_groups, 0);
        }
        else if (m_backPresses < 1)
        {
            final long DELAY = 2000;
            displayToast(getString(R.string.press_one_time_to_exit), Toast.LENGTH_SHORT);
            m_backPresses++;
            android.os.Handler mHandler = getWindow().getDecorView().getHandler();
            mHandler.postAtTime(() ->
            {
                m_backPresses = 0;
            }, android.os.SystemClock.uptimeMillis() + DELAY);
        }
        else
        {
            super.onBackPressed();
        }
    }
    private enum Job
    {
        ADD_ITEM,
        REMOVE_ITEM,
        CHANGE_ITEM
    }
}
