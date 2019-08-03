package com.example.damjan.programzanavodnjavanje.viewHolders;

import android.view.View;
import android.widget.SeekBar;

/***
 * Every view holder must implement this interface
 * otherwise chaos will ensure
 */
public interface IHolder
{
    <T>
    void setListener(T listener);
    <T>
    void updateUI(T data);
}
