package com.example.damjan.programzanavodnjavanje.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.HelpingHand;
import com.example.damjan.programzanavodnjavanje.ActivityHelper.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.listeners.ValveOptionsViewOnClick;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;

import java.util.ArrayList;


public class ValveOptionAdapter extends RecyclerView.Adapter<ValveOptionViewHolder>
{
    private ArrayList<ValveOptionsData> m_valveOptionsArray;
    private HelpingHand                 m_helper;

    public ValveOptionAdapter(ArrayList<ValveOptionsData> valveOptionsArray, @Nullable HelpingHand helper)
    {
        this.m_valveOptionsArray            = valveOptionsArray;
        this.m_helper                       = helper;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ValveOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Context context         = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View valveOptionsView   = inflater.inflate(R.layout.fragment_valve_option, parent, false);

        return new ValveOptionViewHolder(valveOptionsView, m_helper, m_helper == null);
    }

    @Override
    public void onBindViewHolder(@NonNull ValveOptionViewHolder holder, int position)
	{
	    if(m_helper != null)
            holder.setListener(new ValveOptionsViewOnClick(holder, m_helper));
        ValveOptionsData data = m_valveOptionsArray.get(holder.getAdapterPosition());
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
