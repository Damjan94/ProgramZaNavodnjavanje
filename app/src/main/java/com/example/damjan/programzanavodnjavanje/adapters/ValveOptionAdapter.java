package com.example.damjan.programzanavodnjavanje.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.example.damjan.programzanavodnjavanje.IComm;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.listeners.ValveOptionsViewOnClick;
import com.example.damjan.programzanavodnjavanje.listeners.ValveOptionsViewOnSeekChange;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;

import java.util.ArrayList;


public class ValveOptionAdapter extends RecyclerView.Adapter<ValveOptionViewHolder> {
    private ArrayList<ValveOptionsData> m_valveOptionsArray;
    private Activity m_activity;

    public ValveOptionAdapter(ArrayList<ValveOptionsData> valveOptionsArray, Activity activity) {
        this.m_valveOptionsArray = valveOptionsArray;
        m_activity = activity;
        setHasStableIds(true);
    }

    @Override
    public ValveOptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View valveOptionsView = inflater.inflate(R.layout.fragment_valve_option, parent, false);
	
		ValveOptionViewHolder holder = new ValveOptionViewHolder(valveOptionsView, m_activity);
	
		return holder;
    }

    @Override
    public void onBindViewHolder(ValveOptionViewHolder holder,  int position)
	{
		ValveOptionsViewOnClick clickListener = new ValveOptionsViewOnClick(holder, m_activity);
		holder.getSeekPercent().setOnSeekBarChangeListener(new ValveOptionsViewOnSeekChange(m_activity, holder));
		
		for(int i = 0; i < holder.getDayCheck().length; i++)
		{
			holder.getDayCheck()[i].setOnClickListener(clickListener);
		}
		holder.getMasterSwitch().setOnClickListener(clickListener);
		holder.getNumPercent().setOnClickListener(clickListener);
		holder.getTimeView().setOnClickListener(clickListener);
		holder.getTimeCountdown().setOnClickListener(clickListener);
		holder.getValveName().setOnClickListener(clickListener);
		holder.getValveNumber().setOnClickListener(clickListener);
        
        ValveOptionsData data = m_valveOptionsArray.get(position);
        holder.updateUI(data);
    }

    @Override
    public long getItemId(int position)
    {
        return m_valveOptionsArray.get(position).getID();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.fragment_valve_option;
    }

    @Override
    public int getItemCount() {
        return m_valveOptionsArray.size();
    }
}
