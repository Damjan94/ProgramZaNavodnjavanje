package com.example.damjan.programzanavodnjavanje.viewHolders;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.damjan.programzanavodnjavanje.BuildConfig;
import com.example.damjan.programzanavodnjavanje.IComm;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

/**
 * Created by damjan on 3/18/18.
 */

public class ValveOptionViewHolder extends RecyclerView.ViewHolder{
	
	private TextView m_timeView;
	private TextView m_timeCountdown;
	private TextView m_valveName;
	private TextView m_valveNumber;
	
	private Switch m_masterSwitch;
	private CheckBox[] m_dayCheck;
	
	private Activity m_activity;
	
	public TextView getTimeView()
	{
		return m_timeView;
	}
	
	public TextView getTimeCountdown()
	{
		return m_timeCountdown;
	}
	
	public TextView getValveName()
	{
		return m_valveName;
	}
	
	public TextView getValveNumber()
	{
		return m_valveNumber;
	}
	
	public Switch getMasterSwitch()
	{
		return m_masterSwitch;
	}
	
	public CheckBox[] getDayCheck()
	{
		return m_dayCheck;
	}
    
    public ValveOptionViewHolder(View itemView, Activity activity) {
        super(itemView);
        
        m_activity = activity;
        
		m_masterSwitch = itemView.findViewById(R.id.switchMaster);
		m_timeView = itemView.findViewById(R.id.textViewTime);
		m_timeCountdown = itemView.findViewById(R.id.textViewTimeCountdown);
		m_valveName = itemView.findViewById(R.id.textViewValveName);
		m_valveNumber = itemView.findViewById(R.id.textViewValveNumber);
		m_dayCheck = new CheckBox[]
				{
						itemView.findViewById(R.id.checkBoxSun),
						itemView.findViewById(R.id.checkBoxMon),
						itemView.findViewById(R.id.checkBoxTue),
						itemView.findViewById(R.id.checkBoxWed),
						itemView.findViewById(R.id.checkBoxThu),
						itemView.findViewById(R.id.checkBoxFri),
						itemView.findViewById(R.id.checkBoxSat)
						
				};
	
	
		//TODO move this somewhere else
		itemView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(m_activity);
				String msg = m_activity.getResources().getString(R.string.confirm_valve_option_delete);
				msg += " "+ ValveGroup.groups.get(0).getValveOptionDataCollection().get(getAdapterPosition()).getValveNumber();
				msg +=".";
				alertDialog.setMessage(msg);
				alertDialog.setPositiveButton(R.string.positive_response, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						((IComm)m_activity).removeItem(getAdapterPosition());
					}
				});
				alertDialog.setNegativeButton(R.string.negative_response, null);
				alertDialog.show();
				return true;
			}
		});
        
    }

    //update UI skipping any views that already have the same value
    public void updateUI(ValveOptionsData data)
	{
        String time = data.getHours()+":"+data.getMinutes();
        if(!(m_timeView.getText().toString().equals(time)))
		{
			m_timeView.setText(time);
		}
  
		int countdownTime;
		try
		{
			countdownTime = Integer.parseInt(m_timeCountdown.getText().toString());
		}catch(NumberFormatException ex)
		{
			countdownTime = -1;
		}
		if(countdownTime != data.getTimeCountdown())
		{
			m_timeCountdown.setText(String.valueOf(data.getTimeCountdown()));
		}
        
        if(!(m_valveName.getText().toString().equals(data.getValveName())))
		{
			m_valveName.setText(data.getValveName());
		}
		
		int valveNumber;
		try
		{
			valveNumber = Integer.parseInt(m_valveNumber.getText().toString());
		}catch(NumberFormatException ex)
		{
			valveNumber = -1;
		}
		if(valveNumber != data.getValveNumber())
		{
			this.m_valveNumber.setText(String.valueOf(data.getValveNumber()));
		}

		m_masterSwitch.setChecked(data.isMasterSwitch());
        boolean[] days = data.getRepeatDays();
        if( BuildConfig.DEBUG &&(days.length != m_dayCheck.length))
		{
			throw new AssertionError();
		}

        for (int i = 0; i < days.length;i++)
        {
        	//only update the checkbox if it has changed
            if(m_dayCheck[i].isChecked() != days[i])
			{
				m_dayCheck[i].setChecked(days[i]);
			}
        }
    }
}
