package com.example.damjan.programzanavodnjavanje.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.damjan.programzanavodnjavanje.data.bluetooth.ArduinoComms;
import com.example.damjan.programzanavodnjavanje.viewHolders.IHolder;

import java.util.Set;

public class ConnectibleDevicesAdapter extends RecyclerView.Adapter<ConnectibleDevicesAdapter.DevicesViewHolder>
{
    Set<BluetoothDevice> m_devices;
    public ConnectibleDevicesAdapter(Set<BluetoothDevice> bondedDevices)
    {
        m_devices = bondedDevices;
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        TextView text = new TextView(parent.getContext());
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
        return new DevicesViewHolder(text);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesViewHolder holder, int position)
    {
        final BluetoothDevice[] devices = m_devices.toArray(new BluetoothDevice[0]);
        holder.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ArduinoComms.connect(devices[holder.getAdapterPosition()]);
            }
        });
        holder.updateUI(devices[holder.getAdapterPosition()].getName());
    }

    @Override
    public int getItemCount() {
        return m_devices.size();
    }



    class DevicesViewHolder extends RecyclerView.ViewHolder implements IHolder {
        TextView m_textView;
        public DevicesViewHolder(TextView textView)
        {
            super(textView);
            m_textView = textView;
            //textView.setEnabled(false);
        }

        @Override
        public <T> void setListener(T listener)
        {
            m_textView.setOnClickListener((View.OnClickListener) listener);
        }

        @Override
        public <T> void updateUI(T data)
        {
            m_textView.setText((String) data);
        }
    }
}
