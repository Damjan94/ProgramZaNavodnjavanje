package com.example.damjan.programzanavodnjavanje.ActivityHelper;

import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.util.ArrayList;

/**
 * Created by damjan on 3/16/18.
 */

public interface ISetValveData
{
    void setTime(byte hour, byte minute, int viewHolderPosition);
    void setTimeCountdown(int countdownMinutes, int viewHolderPosition);
    void setValveNumber(byte num, int viewHolderPosition);
    void setValveName(String name, int viewHolderPosition);

    void sendValves();

    void removeValve(int viewHolderPosition);

    void addValve(ValveOptionsData item);

}
