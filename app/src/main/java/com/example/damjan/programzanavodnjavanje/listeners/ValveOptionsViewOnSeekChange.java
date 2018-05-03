package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.widget.SeekBar;

import com.example.damjan.programzanavodnjavanje.IComm;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;


public class ValveOptionsViewOnSeekChange implements SeekBar.OnSeekBarChangeListener
{
	Activity m_activity;
	ValveOptionViewHolder m_holder;
	public ValveOptionsViewOnSeekChange(Activity activity, ValveOptionViewHolder holder)
	{
		m_activity = activity;
		m_holder = holder;
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		int newPercentage = seekBar.getProgress();
		((IComm) m_activity).setValvePercentage(newPercentage, m_holder.getAdapterPosition());
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
	
	}
}
