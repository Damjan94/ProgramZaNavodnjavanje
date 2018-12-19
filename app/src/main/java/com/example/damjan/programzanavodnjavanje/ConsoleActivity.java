package com.example.damjan.programzanavodnjavanje;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.damjan.programzanavodnjavanje.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.bluetooth.IBluetoothComms;
import com.example.damjan.programzanavodnjavanje.data.MyCalendar;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ConsoleActivity extends AppCompatActivity implements IBluetoothComms
{

	private final static StringBuilder LOG = new StringBuilder();

	private static TextView console;

	public static void log(String msg)
	{
		LOG.append(msg);
		if(console != null)
			console.post(()-> console.setText(LOG));

	}

	@Override
	protected void onStart()
	{
		super.onStart();
		ArduinoComms.registerListener(this);
	}
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);

		console = findViewById(R.id.consoleTextView);
		console.setMovementMethod(new ScrollingMovementMethod());
		console.setText(LOG);


		EditText sendCommand = findViewById(R.id.consoleTextInput);
		Button refresh = findViewById(R.id.consoleButton);
		refresh.setOnClickListener(v->
		{
			handleCommand(new String[] {sendCommand.getText().toString()});
		});
		refresh.setOnLongClickListener(v->
		{
			LOG.delete(0, LOG.length());
			console.setText(LOG);
			return true;
		});

	}

	private void handleCommand(String[] commands)
	{
		if(commands[0].equals("valves read"))
		{
			ArduinoComms.getValves();
		}
		else if(commands[0].equals("temp read"))
		{
			ArduinoComms.getTempFloat();
		}
		else if(commands[0].equals("time read"))
		{
			ArduinoComms.getTime();
		}
		else if(commands[0].equals("time send"))
		{
			final String PATTERN = "yyyy MM dd HH:mm";
			MyCalendar cal = new MyCalendar();
			cal.setFirstDayOfWeek(Calendar.SUNDAY);
			if(commands.length > 1) {
				SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.ENGLISH);
				try {
					cal.setTime(sdf.parse(commands[1]));// all done
				} catch (ParseException e) {
					log("failed to parse date. The format should be ascii, with pattern " + PATTERN + '\n' +
							"got " + commands[1] + '\n');
				}
			}
			ArduinoComms.sendTime(cal);
		}
		else if(commands[0].equals("hbridge read"))
		{
			ArduinoComms.getHBridgePin();
		}
		else
		{
			log("unknown command: \""+commands[0]+'"');
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		ArduinoComms.unregisterListener(this);
		console = null;
	}

	@Override
	public void connected()
	{
		log("Connected\n");
	}

	@Override
	public void connectionFailed()
	{
		log("Connection Failed\n");
	}

	@Override
	public void disconnected()
	{
		log("Disconnected\n");
	}

	@Override
	public void setTemperature(float temperature)
	{
		log("Temperature: " + temperature+'\n');
	}

	@Override
	public void setTime(Calendar time)
	{
		log("Time:\n" + time.toString());
	}

	@Override
	public void setValves(ValveOptionsData[] valves)
	{
		log("Arduino Valves:\n");
		for(ValveOptionsData valve : valves)
		{
			log(valve.toString());
		}
	}
}
