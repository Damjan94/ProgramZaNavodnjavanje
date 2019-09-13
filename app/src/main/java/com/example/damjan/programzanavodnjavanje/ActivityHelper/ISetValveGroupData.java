package com.example.damjan.programzanavodnjavanje.ActivityHelper;

import com.example.damjan.programzanavodnjavanje.data.ValveGroup;

public interface ISetValveGroupData
{
    void addGroup(ValveGroup group);

    void removeGroup(int viewHolderPosition);

    void setGroupPercent(int newPercent, int viewHolderPosition);

    void groupSelected(int viewHolderPosition);

    void setGroupName(String name, int viewHolderPosition);
}
