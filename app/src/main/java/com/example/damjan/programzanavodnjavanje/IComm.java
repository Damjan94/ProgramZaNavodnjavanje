package com.example.damjan.programzanavodnjavanje;

import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.util.ArrayList;

/**
 * Created by damjan on 3/16/18.
 */

public interface IComm {
    void setTime(int hour, int minute, int viewHolderPosition);
    void setValveNumber(int num, int viewHolderPosition);
    void setValveName(String name, int viewHolderPosition);
    void setValvePercentage(int percentage, int viewHolderPosition);
    void setValveDayOn(boolean[] days, int viewHolderPosition);
    void setValveDayOn(boolean value, int day, int viewHolderPosition);
    void setMasterSwitch(boolean value, int viewHolderPosition);
    
    void removeItem(int viewHolderPosition);
    void addItem(ValveOptionsData item);
    void changeDataSet(ArrayList<ValveOptionsData> data);
}
