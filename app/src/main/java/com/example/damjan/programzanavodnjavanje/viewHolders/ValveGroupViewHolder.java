package com.example.damjan.programzanavodnjavanje.viewHolders;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.damjan.programzanavodnjavanje.IMainActivity;
import com.example.damjan.programzanavodnjavanje.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.utility.Dialogs;

import java.util.Locale;

public class ValveGroupViewHolder extends RecyclerView.ViewHolder implements IHolder
{
    private TextView    m_groupName;
    private TextView    m_valvesInGroup;

    private SeekBar     m_seekBar;
    private TextView    m_percent;

    private Switch      m_masterSwitch;

    public <T extends Activity & ISetValveData> ValveGroupViewHolder(View itemView, T activity)
    {
        super(itemView);

        m_groupName                 = itemView.findViewById(R.id.valve_group_name);
        TextView valvesInGroupText  =itemView.findViewById(R.id.textViewName);
        valvesInGroupText.setText(R.string.valves_in_this_group);
        m_valvesInGroup             = itemView.findViewById(R.id.textViewNumber);

        m_seekBar                   = itemView.findViewById(R.id.valve_group_seekBar);
        m_percent                   = itemView.findViewById(R.id.valve_group_textView);

        m_masterSwitch              = itemView.findViewById(R.id.switchMaster);

        itemView.setOnClickListener((view) ->
        {
            activity.groupSelected(getAdapterPosition());
        });


    }


    @Override
    public <T> void setListener(T listener)
    {
        m_seekBar.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) listener);
        m_percent.setOnClickListener((View.OnClickListener) listener);
        m_groupName.setOnClickListener((View.OnClickListener) listener);

    }

    @Override
    public <T> void updateUI(T data)
    {
        ValveGroup groupData = (ValveGroup)data;

        m_groupName.setText(groupData.getGroupName());
        m_valvesInGroup.setText(String.format(Locale.getDefault(),"%d", groupData.size()));

        int percent = groupData.getPercent();
        if(m_seekBar.getProgress() != percent)
            m_seekBar.setProgress(percent);
        if(!(m_percent.getText().length() > 0) || Integer.parseInt(m_percent.getText().toString()) != percent)
            m_percent.setText(String.format(Locale.getDefault(),"%d", percent));

        m_masterSwitch.setChecked(groupData.getStatus());

    }
}
