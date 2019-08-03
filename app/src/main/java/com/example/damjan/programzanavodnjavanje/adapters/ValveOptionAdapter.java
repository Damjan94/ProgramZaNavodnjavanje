package com.example.damjan.programzanavodnjavanje.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.listeners.ValveOptionsViewOnClick;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveOptionViewHolder;

import java.util.ArrayList;


public class ValveOptionAdapter extends RecyclerView.Adapter<ValveOptionViewHolder> {
    private ArrayList<ValveOptionsData> m_valveOptionsArray;
    private Activity                    m_activity;
    private SaveFile                    m_saveFile;
    public ValveOptionAdapter(ArrayList<ValveOptionsData> valveOptionsArray, Activity activity, SaveFile saveFile) {
        this.m_valveOptionsArray         = valveOptionsArray;
        this.m_activity                  = activity;
        this.m_saveFile                  = saveFile;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ValveOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context         = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View valveOptionsView   = inflater.inflate(R.layout.fragment_valve_option, parent, false);

        return new ValveOptionViewHolder(valveOptionsView, m_activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ValveOptionViewHolder holder, int position)
	{
        holder.setListener(new ValveOptionsViewOnClick(holder, m_activity, m_saveFile));
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
