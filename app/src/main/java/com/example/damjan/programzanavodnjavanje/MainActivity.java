package com.example.damjan.programzanavodnjavanje;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.HelpingHand;
import com.example.damjan.programzanavodnjavanje.ActivityHelper.IMainActivity;
import com.example.damjan.programzanavodnjavanje.ActivityHelper.ISetValveData;
import com.example.damjan.programzanavodnjavanje.adapters.ValveGroupAdapter;
import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
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
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity
{
    private final static String SAVE_FILE_NAME      = "my_file.json";
    private final static long   VIBRATION_DURATION  = 250;

    //What data is being used to populate the recycler view
    public enum AdapterState
    {
        NONE,
        VALVES,
        VALVE_GROUPS,
        VALVES_TO_SEND,
        VALVES_ON_ARDUINO,
        ERRORS,
        CONNECT_BLUETOOTH
    }
    private RecyclerView    m_RecyclerView;

    private Menu            m_drawerMenu;

    private AdapterState    m_adapterState;

    private HelpingHand     m_helper;

    private ImageButton         m_getTempButton;
    private ArrayList<MenuItem> m_buttonsToToogleOnConnection;
    private MenuItem            m_connectBluetooth;
    private MenuItem            m_disconnect;
    private MenuItem            m_syncBluetooth;
    private MenuItem            m_valvesOnArduino;
    private MenuItem            m_showErrors;

    private TextView        m_temperatureText;
    private TextView        m_dateTimeText;

    private TextView        m_selectedGroupName;


    private static int m_backPresses = 0;
    private DrawerLayout m_drawerLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        m_disconnect        = menu.findItem(R.id.disconnect);
        m_valvesOnArduino   = menu.findItem(R.id.valves_on_arduino);

        MenuItem addValve   = menu.findItem(R.id.add_valve);
        ImageButton addValveImage = new ImageButton(this);
        addValveImage.setImageResource(R.drawable.ic_add_circle_outline_black_24dp);
        addValveImage.setBackgroundColor(Color.TRANSPARENT);
        addValveImage.setOnLongClickListener((v) ->
        {
            m_helper.addGroup(new ValveGroup(getString(R.string.new_group)));
            if(m_adapterState == AdapterState.VALVE_GROUPS)//don't notify the adapter, if it's not the right one
                m_RecyclerView.getAdapter().notifyItemInserted(m_helper.getGroupsSize() -1);
            if(m_helper.getGroupsSize() <= 2)//means we just added the first or second group, which is a special condition
                onBackPressed();//so we should start displaying the Valve groups instead of just valves
            return true;
        });
        addValveImage.setOnClickListener((v) ->
        {
            m_helper.addValve(new ValveOptionsData(getString(R.string.valve)));
        });
        addValve.setActionView(addValveImage);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.disconnect:
            {
                ArduinoComms.disconnect();
                break;
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        m_adapterState = AdapterState.NONE;

        m_selectedGroupName     = findViewById(R.id.selected_view_group_name);
        m_selectedGroupName.setOnClickListener((v) ->
        {
            if(m_adapterState != AdapterState.VALVES)
                return;
            DialogFragment namePicker = new Dialogs.NamePicker();
            Bundle bundle = new Bundle();
            bundle.putInt(Dialogs.POSITION, -1);
            namePicker.setArguments(bundle);
            namePicker.show(getFragmentManager(), "namePicker");
        });

        m_drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView  = findViewById(R.id.nav_view);
        
        m_drawerMenu            = navView.getMenu();
        m_connectBluetooth      = m_drawerMenu.findItem(R.id.connect_bluetooth);
        m_syncBluetooth         = m_drawerMenu.findItem(R.id.sync_bluetooth);
        m_showErrors            = m_drawerMenu.findItem(R.id.show_errors);
        navView.setNavigationItemSelectedListener(new NavigationItemSelectedListener(m_helper);
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
        m_saveFile.executeWhenDoneReading(this::showMainMenu);
        m_saveFile.read();
        //TODO notify us that we have read the data, but make sure not to do it while we are paused
        ArduinoComms.registerListener(this);
        ArduinoComms.registerListener(m_saveFile);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

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

    @Override
    public void onBackPressed()
    {
        if((m_saveFile.getGroups().size() > 1 && m_adapterState != AdapterState.VALVE_GROUPS) ||
                (m_saveFile.getGroups().size() == 1 && m_adapterState != AdapterState.VALVES))
        {
            showMainMenu();
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


    private void showMainMenu()
    {
        if(m_saveFile != null && m_saveFile.getGroups().size() > 1)
            showValveGroups();
        else
            groupSelected(0);
    }

    private void showValveGroups()
    {
        m_selectedGroupName.setText("");
        ValveGroupAdapter adapter = new ValveGroupAdapter<>(m_saveFile.getGroups(), this, m_saveFile);
        adapter.setHasStableIds(false);
        m_saveFile.getGroups().selectedGroup = ValveGroups.INVALID_SELECTED_GROUP;
        setAdapter(adapter, AdapterState.VALVE_GROUPS);
    }
    private enum Job
    {
        ADD_ITEM,
        REMOVE_ITEM,
        CHANGE_ITEM
    }
}
