package com.example.damjan.programzanavodnjavanje.listeners;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.HelpingHand;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;


public class ValveOptionsViewOnClick implements View.OnClickListener
{
	private ValveOptionViewHolder 	m_viewHolder;
	private HelpingHand				m_helper;

	public ValveOptionsViewOnClick(ValveOptionViewHolder holder, HelpingHand helper)
	{
		m_viewHolder 	= holder;
		m_helper		= helper;
	}
	@Override
	public void onClick(View v)
	{
		int adapterPosition = m_viewHolder.getAdapterPosition();
		int day;
		switch(v.getId())
		{
			case R.id.textViewTime:
			{
				m_helper.showTimePicker(adapterPosition);
				break;
			}
			case R.id.textViewTimeCountdown:
			{
				m_helper.showMinutePicker(adapterPosition);
				break;
			}
			case R.id.textViewName:
			{
				m_helper.showNamePicker(adapterPosition);
				break;
			}
			case R.id.textViewNumber:
			{
				m_helper.showNumberPicker(adapterPosition);
				break;
			}
			case R.id.switchMaster:
			{
				m_helper.setValveMasterSwitch(adapterPosition, m_viewHolder.getMasterSwitch().isChecked());
				break;
			}
			case R.id.checkBoxSun:
				day = 0;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxMon:
				day = 1;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxTue:
				day = 2;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxWed:
				day = 3;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxThu:
				day = 4;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxFri:
				day = 5;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
			case R.id.checkBoxSat:
				day = 6;
				m_helper.setDayCheckBox(adapterPosition, m_viewHolder.getDayCheck()[day].isChecked(), day);
				break;
		}
	}
}
