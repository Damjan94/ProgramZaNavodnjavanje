package com.example.damjan.programzanavodnjavanje.viewHolders;

/***
 * Every view holder must implement this interface
 */
public interface IHolder
{
    <T>
    void setListener(T listener);
    <T>
    void updateUI(T data);
}
