package com.example.damjan.programzanavodnjavanje;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.bluetooth.BluetoothComms;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements IComm{

	public final static int VERSION_NUMBER = 0;
	public final static String VERSION_STRING = "Version";
	private final static String ARRAY_OF_VALVES_STRING = "ArrayOfValves";
	
	final byte SEND_VALVE = 0x2c;
	final byte RECEIVE_VALVE = 0x1c;
	
	private boolean m_isResumeExecuted = false;
	private Thread m_loadFileThread = null;
	private final int REQUEST_ENABLE_BT = 10;
	private BluetoothComms m_blueComms;

    Button logValves;
	
	final String CLOCK_HOUR = "Hour";
	final String CLOCK_MINUTE = "Minute";
	final String CLOCK_SECOND = "Second";
	final String CLOCK_DAY = "Day";
	final String CLOCK_DAY_OF_WEEK = "DayOfWeek";
	final String CLOCK_MONTH = "Month";
	final String CLOCK_YEAR = "Year";
	
	
	private enum Job
	{
		ADD_ITEM,
		REMOVE_ITEM,
		CHANGE_ITEM
	}
	
	private final static  String SAVE_FILE_NAME = "my_file.json";
    public static Activity mainActivity;
	
    private RecyclerView m_RecyclerView;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.addValveButton:
			{
				addItem(new ValveOptionsData());
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	//TODO this is a quick hack. get rid of it!
	Button syncBluetooth;
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        Button simulateSend = new Button(this);
        simulateSend.setText("Simulate Sending");
        simulateSend.setOnClickListener(v ->
        {
            for(ValveOptionsData d : ValveOptionsData.getValveOptionDataCollection())
            {
                d.getBytesForArduino();
            }
        });

		addContentView(simulateSend, new ViewGroup.LayoutParams(500, 250));
		m_blueComms = new BluetoothComms();
		
		m_RecyclerView = findViewById(R.id.mainFragmentHolder);
		m_RecyclerView.setHasFixedSize(true);
		
		LinearLayoutManager linearLayout = new LinearLayoutManager(this);
		linearLayout.setOrientation(LinearLayoutManager.VERTICAL);
		m_RecyclerView.setLayoutManager(linearLayout);
		
		ValveOptionAdapter adapter = new ValveOptionAdapter(ValveOptionsData.getValveOptionDataCollection(), this);
		m_RecyclerView.setAdapter(adapter);
		
		DividerItemDecoration decoration = new DividerItemDecoration(this, linearLayout.getOrientation());
		m_RecyclerView.addItemDecoration(decoration);
		((DefaultItemAnimator) m_RecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
 	
		logValves = findViewById(R.id.logValvesFromArduino);
		logValves.setOnClickListener(v ->
		{
			try
			{
				
				//use below to retrieve the valves from arduino
				
				m_blueComms.write(RECEIVE_VALVE);
				
				byte size = m_blueComms.read();
				for(int i = 0; i < size; i++)
				{
					byte[] valveFromArduino = new byte[ValveOptionsData.VALVE_DATA_NETWORK_SIZE];
					for (int j = 0; j < valveFromArduino.length; j++)
					{
						valveFromArduino[j] = m_blueComms.read();
					}
					
					int valveNumber = valveFromArduino[0];
					byte hour = valveFromArduino[1];
					byte minute = valveFromArduino[2];
					byte daysOn = valveFromArduino[3];
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(valveFromArduino[4]);
					bb.put(valveFromArduino[5]);
					short timeContdown = bb.getShort(0);
					boolean[] repeatDays = new boolean[7];
					for(int k = 0; k< repeatDays.length; k++)
					{
						repeatDays[k] = ((daysOn >> k+1) & 0x1) == 1;
					}
					ValveOptionsData valve = new ValveOptionsData(
							MainActivity.mainActivity.getResources().getString(R.string.valve) + valveNumber,
							valveNumber,
							100,
							hour,
							minute,
							timeContdown,
							repeatDays,
							false
					);
					try
					{
						Log.i("Received Valve", valve.toJson().toString(1));
					} catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
				
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});
        logValves.setEnabled(false);
		
		syncBluetooth = findViewById(R.id.bluetoothSync);
		syncBluetooth.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final ArrayList<ValveOptionsData> valveOptionDataCollection = ValveOptionsData.getValveOptionDataCollection();
				m_blueComms.write(SEND_VALVE);
				m_blueComms.write(valveOptionDataCollection.size());
				for(ValveOptionsData data : valveOptionDataCollection)
				{
					byte[] arr = data.getBytesForArduino();
					for (byte anArr : arr)
					{
						m_blueComms.write(anArr);
					}
					Log.i("brake","test");
					//todo wait for an response before sending another valve
				}
			}
    	});
		syncBluetooth.setEnabled(false);
    }
    
    //TODO get rid of the hack below!!!
	@Override
	public void enableSendButton()
	{
		android.os.Handler mHandler = getWindow().getDecorView().getHandler();
		mHandler.post(() ->
                {
				syncBluetooth.setEnabled(true);
                logValves.setEnabled(true);
                });
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		//read saved data
		m_loadFileThread = new Thread(() ->
		{
			ArrayList<ValveOptionsData> newData = null;
			try (FileInputStream fileInputStream = openFileInput(SAVE_FILE_NAME))
			{
				newData = readDataFromFile(fileInputStream);
			} catch (IOException e)
			{
				Log.e("Read from disk: ", e.toString());
			}
			finally
			{
				synchronized (m_loadFileThread)
				{
					try
					{
						if(!m_isResumeExecuted)
							m_loadFileThread.wait();
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					finally
					{
						((IComm)mainActivity).changeDataSet(newData);
					}
				}
			}
			
		}, "File loader thread");
		m_loadFileThread.start();
		
	}
	
	@Override
	protected  void onResume()
	{
		super.onResume();
		synchronized (m_loadFileThread)
		{
			m_isResumeExecuted = true;
			m_loadFileThread.notify();
		}
		//get bluetooth stuff done
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		synchronized (m_loadFileThread)
		{
			m_isResumeExecuted = false;
		}
		//release bluetooth stuff
		
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		
		//save stuff
		Thread saveData = new Thread(() ->
		{
			try(FileOutputStream fileOutputStream = openFileOutput(SAVE_FILE_NAME, MODE_PRIVATE))
			{
				//TODO change  write to file method to accept the data
				writeDataToFile(fileOutputStream);
			} catch (IOException e)
			{
				Log.w("write: ", e);
			}
		});
		saveData.start();
	}
	
	
	private void writeDataToFile(FileOutputStream fileOutputStream) throws IOException
	{
		JSONArray arr = new JSONArray();
		for(ValveOptionsData data : ValveOptionsData.getValveOptionDataCollection())
		{
			
			arr.put(data.toJson());
		}
		
		JSONObject object = new JSONObject();
		try
		{
			object.put(VERSION_STRING, VERSION_NUMBER);
			object.put(ARRAY_OF_VALVES_STRING, arr);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		byte[] objBytes = object.toString().getBytes();
		fileOutputStream.write(objBytes);
	}
	
	private ArrayList<ValveOptionsData> readDataFromFile(FileInputStream fileInputStream) throws IOException
	{
		ArrayList<ValveOptionsData> data = new ArrayList<>();
		int availableBytes = fileInputStream.available();
		byte[] inStream;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(availableBytes);
		while(availableBytes > 0)
		{
			inStream = new byte[availableBytes];
			fileInputStream.read(inStream);
			outputStream.write(inStream);
			
			availableBytes = fileInputStream.available();
		}

		try
		{
			String jsonString = new String(outputStream.toByteArray());
			JSONObject obj = new JSONObject(jsonString);
			int version = obj.getInt(VERSION_STRING);
			if(version != VERSION_NUMBER)
			{
				Toast.makeText(this, "Save file version missmatch!", Toast.LENGTH_SHORT).show();
			}
			JSONArray valveObjects = obj.getJSONArray(ARRAY_OF_VALVES_STRING);
			
			for (int i = 0; i < valveObjects.length(); i++)
			{
				data.add(ValveOptionsData.fromJSON(valveObjects.getJSONObject(i)));
			}
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return data;
	}
	
	void notifyItemChanged(final int pos, final Job job, final ValveOptionsData data)
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
	public void addItem(ValveOptionsData item)
	{
		ValveOptionsData.addValveOptionData(item);
		notifyItemChanged(ValveOptionsData.getValveOptionDataCollection().size()-1, Job.ADD_ITEM, null);
	}
	
	@Override
	public void removeItem(int viewHolderPosition)
	{
		if(viewHolderPosition == RecyclerView.NO_POSITION)
			return;
		
		ValveOptionsData.removeValveOptionData(viewHolderPosition);
		notifyItemChanged(viewHolderPosition, Job.REMOVE_ITEM, null);
	}
	
	@Override
    public void setTime(int hour, int minute, int viewHolderPosition)
    {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        
        switch (hour)
        {
            case -1 :
            {
                data.setTimeCountdown(minute);
                data.setPercentage(100);
                notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
                break;
            }

            default:
            {
                data.setTime(hour, minute);
				notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
                break;
            }
        }
    }

    @Override
    public void setValveNumber(int num, int viewHolderPosition)
    {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setValveNumber(num);
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
    }

    @Override
    public void setValveName(String name, int viewHolderPosition)
    {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setValveName(name);
	
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
    }

    @Override
    public void setValvePercentage(int percentage, int viewHolderPosition) {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setPercentage(percentage);
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);

    }

    @Override
    public void setValveDayOn(boolean[] days, int viewHolderPosition) {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setRepeatDay(days);
		
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
    }

    @Override
    public void setValveDayOn(boolean value, int day, int viewHolderPosition) {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setRepeatDay(value, day);
	
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
    }

    @Override
    public void setMasterSwitch(boolean value, int viewHolderPosition)
    {
        if(viewHolderPosition == RecyclerView.NO_POSITION)
            return;
        ValveOptionsData data = ValveOptionsData.getValveOptionData(viewHolderPosition);
        if(data == null)
            return;
        data.setMasterSwitch(value);
	
		notifyItemChanged(viewHolderPosition, Job.CHANGE_ITEM, data);
    }
	
	@Override
	public void changeDataSet(ArrayList<ValveOptionsData> data)
	{
		if(data == null || data.size() < 1)
		{
			return;
		}
		MainActivity.this.runOnUiThread(() ->
		{
			ValveOptionsData.setValveOptionDataCollection(data);
			m_RecyclerView.swapAdapter(new ValveOptionAdapter(data, this), false);
		});
		
	}
}
