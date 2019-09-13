package com.example.damjan.programzanavodnjavanje.ActivityHelper;

import android.support.v7.widget.RecyclerView;

import com.example.damjan.programzanavodnjavanje.MainActivity;

public interface IMainActivity
{
    void setAdapter(RecyclerView.Adapter adapter, MainActivity.AdapterState state);

    void adapterItemChanged(int pos);

    void showTemperature();

    void showToast(String text, int length);

    void showToast(int resID, int length);

    void showAlertDialog(int message);
}

