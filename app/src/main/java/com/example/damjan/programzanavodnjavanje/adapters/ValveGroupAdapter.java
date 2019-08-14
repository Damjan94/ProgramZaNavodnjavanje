package com.example.damjan.programzanavodnjavanje.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damjan.programzanavodnjavanje.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.file.SaveFile;
import com.example.damjan.programzanavodnjavanje.listeners.ValveGroupListener;
import com.example.damjan.programzanavodnjavanje.viewHolders.IHolder;
import com.example.damjan.programzanavodnjavanje.viewHolders.ValveGroupViewHolder;

import java.util.ArrayList;

public class ValveGroupAdapter<T extends Activity & ISetValveData> extends RecyclerView.Adapter<ValveGroupViewHolder>
{
    private final T                 m_activity;
    private ArrayList<ValveGroup>   m_valveGroupArray;
    private SaveFile                m_saveFile;

    public ValveGroupAdapter(ArrayList<ValveGroup> valveGroupArray, T activity, SaveFile saveFile)
    {
        m_valveGroupArray   = valveGroupArray;
        m_activity          = activity;
        m_saveFile          = saveFile;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ValveGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View valveGroupView = inflater.inflate(R.layout.fragment_valve_group, parent, false);
        //TODO set on click listener to select a group to show the valves, and long click listener, to delete this group
        return new ValveGroupViewHolder(valveGroupView, m_activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ValveGroupViewHolder holder, int position)
    {
        holder.setListener(new ValveGroupListener(m_activity, holder, m_saveFile));
        holder.updateUI(m_valveGroupArray.get(position));

    }

    @Override
    public int getItemCount()
    {
        return m_valveGroupArray.size();
    }
}
