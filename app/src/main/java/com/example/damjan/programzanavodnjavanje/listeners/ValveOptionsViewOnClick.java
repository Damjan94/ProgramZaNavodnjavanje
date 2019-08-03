package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.example.damjan.programzanavodnjavanje.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;


public class ValveOptionsViewOnClick implements View.OnClickListener
{
	private ValveOptionViewHolder 	m_viewHolder;
	private Activity 				m_activity;
	private SaveFile				m_saveFile;

	public ValveOptionsViewOnClick(ValveOptionViewHolder holder, Activity activity, SaveFile saveFile)
	{
		m_viewHolder 	= holder;
		m_activity 		= activity;
		m_saveFile		= saveFile;
	}
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.textViewTime:
			{
				DialogFragment timePicker = new Dialogs.TimePicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_viewHolder.getAdapterPosition());
				timePicker.setArguments(bundle);
				timePicker.show(m_activity.getFragmentManager(), "timePicker");
				break;
			}
			case R.id.textViewTimeCountdown:
			{
				DialogFragment minutePicker = new Dialogs.MinutePicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_viewHolder.getAdapterPosition());
				minutePicker.setArguments(bundle);
				minutePicker.show(m_activity.getFragmentManager(), "minutePicker");
				break;
			}
			case R.id.textViewName:
			{
				DialogFragment namePicker = new Dialogs.NamePicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_viewHolder.getAdapterPosition());
				namePicker.setArguments(bundle);
				namePicker.show(m_activity.getFragmentManager(), "namePicker");
				break;
			}
			case R.id.textViewNumber:
			{
				DialogFragment numberPicker = new Dialogs.NumberPicker();
				Bundle bundle = new Bundle();
				bundle.putInt(Dialogs.POSITION, m_viewHolder.getAdapterPosition());
				bundle.putInt(Dialogs.FUNCTION, Dialogs.FUNCTION_UPDATE_NUMBER);
				numberPicker.setArguments(bundle);
				numberPicker.show(m_activity.getFragmentManager(), "numberPicker");
				break;
			}
			case R.id.switchMaster:
			{
				//((ISetValveData)m_activity).setMasterSwitch(m_viewHolder.getMasterSwitch().isChecked(), m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setMasterSwitch(m_viewHolder.getMasterSwitch().isChecked());
				break;
			}
			case R.id.checkBoxSun:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[0].isChecked(), 0, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[0].isChecked(), 0);
				break;
			case R.id.checkBoxMon:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[1].isChecked(), 1, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[1].isChecked(), 1);
				break;
			case R.id.checkBoxTue:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[2].isChecked(), 2, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[2].isChecked(), 2);
				break;
			case R.id.checkBoxWed:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[3].isChecked(), 3, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[3].isChecked(), 3);
				break;
			case R.id.checkBoxThu:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[4].isChecked(), 4, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[4].isChecked(), 4);
				break;
			case R.id.checkBoxFri:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[5].isChecked(), 5, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[5].isChecked(), 5);
				break;
			case R.id.checkBoxSat:
				//((ISetValveData)m_activity).setValveDayOn(m_viewHolder.getDayCheck()[6].isChecked(), 6, m_viewHolder.getAdapterPosition());
				m_saveFile.getGroups().get().get(m_viewHolder.getAdapterPosition()).setRepeatDay(m_viewHolder.getDayCheck()[6].isChecked(), 6);
				break;
		}
	}
}
