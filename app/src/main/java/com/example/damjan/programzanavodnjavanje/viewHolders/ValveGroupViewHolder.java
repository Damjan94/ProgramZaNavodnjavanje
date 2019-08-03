package com.example.damjan.programzanavodnjavanje.viewHolders;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;

public class ValveGroupViewHolder extends RecyclerView.ViewHolder implements IHolder
{
    private TextView    m_groupName;
    private TextView    m_valvesInGroup;

    private SeekBar     m_seekBar;
    private TextView    m_percent;

    private Switch      m_masterSwitch;
    public ValveGroupViewHolder(View itemView, Activity activity)
    {
        super(itemView);

                ((TextView)itemView.findViewById(R.id.textViewName)).setText(R.string.valve_group_name);
        m_groupName     = itemView.findViewById(R.id.textViewNumber);

        m_valvesInGroup = itemView.findViewById(R.id.textViewName);

        m_seekBar       = itemView.findViewById(R.id.valve_group_seekBar);
        m_percent       = itemView.findViewById(R.id.valve_group_textView);

        m_masterSwitch  = itemView.findViewById(R.id.switchMaster);

    }


    @Override
    public <T> void setListener(T listener)
    {
        m_seekBar.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) listener);
        m_percent.setOnClickListener((View.OnClickListener) listener);

    }

    @Override
    public <T> void updateUI(T data)
    {
        ValveGroup groupData = (ValveGroup)data;

        m_groupName.setText(groupData.getGroupName());
        if(m_seekBar.getProgress() != groupData.getPercent())
            m_seekBar.setProgress(groupData.getPercent());
        m_masterSwitch.setChecked(groupData.getStatus());

    }
}
