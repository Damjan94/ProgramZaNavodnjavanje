package com.example.damjan.programzanavodnjavanje;

import android.support.v7.widget.RecyclerView;

public interface IMainActivity
{
    void displayToast(String text, int lenght);
    void sendValves();

    void setAdapter(RecyclerView.Adapter adapter);
    void adapterItemChanged(int pos);

    void showTemperature();

    void showAlertDialog(int message);

    void doOnUiThread(Runnable r);
}
