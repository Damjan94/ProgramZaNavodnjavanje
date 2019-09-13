package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.IMainActivity;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveGroupViewHolder;


public class ValveGroupListener implements SeekBar.OnSeekBarChangeListener, View.OnClickListener
{
	private Activity 				m_activity;
	private ValveGroupViewHolder 	m_holder;
	private SaveFile				m_saveFile;
	public ValveGroupListener(Activity activity, ValveGroupViewHolder holder, SaveFile saveFile)
	{
		m_activity 	= activity;
		m_holder 	= holder;
		m_saveFile 	= saveFile;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		if(!fromUser) return;
		m_saveFile.getGroups().get(m_holder.getAdapterPosition()).setPercent(progress);
		((IMainActivity)m_activity).adapterItemChanged(m_holder.getAdapterPosition());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{

	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
	
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.valve_group_textView:
			{
				DialogFragment numPicker = new Dialogs.NumberPicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_holder.getAdapterPosition());
				bundle.putInt(Dialogs.FUNCTION, Dialogs.FUNCTION_UPDATE_PERCENTAGE);
				numPicker.setArguments(bundle);
				numPicker.show(m_activity.getFragmentManager(), "numberPicker");
				break;
			}
			case R.id.valve_group_name:
			{
				DialogFragment namePicker = new Dialogs.NamePicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_holder.getAdapterPosition());
				namePicker.setArguments(bundle);
				namePicker.show(m_activity.getFragmentManager(), "namePicker");
				break;
			}
		}

	}
}
