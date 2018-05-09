package com.example.damjan.programzanavodnjavanje;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.damjan.programzanavodnjavanje.adapters.ValveOptionAdapter;
import com.example.damjan.programzanavodnjavanje.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.bluetooth.IBluetoothComms;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements IComm, IBluetoothComms{

	public final static int VERSION_NUMBER = 0;
	public final static String VERSION_STRING = "Version";
	private final static String ARRAY_OF_VALVES_STRING = "ArrayOfValves";
	
	private boolean m_isResumeExecuted = false;
	private Thread m_loadFileThread = null;

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
			case R.id.consoleModeButton:
			{
				Intent i = new Intent(this, ConsoleActivity.class);
				startActivity(i);
				return true;
			}
			case R.id.connectBluetooth:
			{
				BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
				if(defaultAdapter == null)
				{
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
					String msg = getString(R.string.no_bluetooth_found);
					alertDialog.setMessage(msg);
					alertDialog.show();
					return true;
				}
				if(!defaultAdapter.isEnabled())
				{
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
					String msg = "Please enable bluetooth, and try again";
					alertDialog.setMessage(msg);
					alertDialog.show();
					return true;
				}

				BluetoothDevice device = defaultAdapter.getBondedDevices().iterator().next();
				ArduinoComms.connect(device);
				return true;
			}
			case R.id.showTemperature:
			{
				ArduinoComms.getTempFloat();
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	void setUpRecyclerView()
	{
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
	}

	//TODO this is a quick hack. get rid of it!
	Button syncBluetooth;

	@Override
	public void connected()
	{
			android.os.Handler mHandler = getWindow().getDecorView().getHandler();
			mHandler.post(() ->
			{
				syncBluetooth.setEnabled(true);
				Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
			});
	}

	@Override
	public void connectionFailed()
	{
		android.os.Handler mHandler = getWindow().getDecorView().getHandler();
		mHandler.post(() ->
		{
			syncBluetooth.setEnabled(true);
			Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
		});
	}

	@Override
	public void disconnected()
	{
		android.os.Handler mHandler = getWindow().getDecorView().getHandler();
		mHandler.post(() ->
		{
			syncBluetooth.setEnabled(false);
			Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
		});
	}

	@Override
	public void setTemperature(float temperature)
	{
		android.os.Handler mHandler = getWindow().getDecorView().getHandler();
		mHandler.post(() ->
		{
			Toast.makeText(this, "temp = "+temperature, Toast.LENGTH_SHORT).show();
		});
	}

	@Override
	public void setTime(Calendar timeAsync)
	{

	}

	@Override
	public void setValves(ValveOptionsData[] valvesAsync)
	{
		for(ValveOptionsData d : valvesAsync)
		{
			try {
				ConsoleActivity.LOG.append(d.toJson().toString(1));
			} catch (JSONException e) {
				ConsoleActivity.LOG.append(e.toString());
			}
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);

		setUpRecyclerView();

		syncBluetooth = findViewById(R.id.bluetoothSync);
		syncBluetooth.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final ArrayList<ValveOptionsData> valveOptionDataCollection = ValveOptionsData.getValveOptionDataCollection();
				ValveOptionsData[] arr = new ValveOptionsData[0];
				arr = valveOptionDataCollection.toArray(arr);
				ArduinoComms.sendValves(arr);
				Toast.makeText(mainActivity, "Sending...", Toast.LENGTH_SHORT).show();
			}
    	});
		syncBluetooth.setEnabled(false);
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

		ArduinoComms.registerListener(this);
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
		//ArduinoComms.disconnect();
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
		ArduinoComms.unregisterListener(this);
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
