package com.example.damjan.programzanavodnjavanje.data.file;

import android.support.annotation.Nullable;

public interface IFileListener
{
    /***
     *
     * @param fileContents if status is true, it is all of the file contents. otherwise, only contents up until failure point
     * @param status true if completed successfully, false otherwise
     */
    void doneReading(@Nullable byte[] fileContents, boolean status);

    /***
     *
     * @param status true if completed successfully, false otherwise
     */
    void doneWriting(boolean status);
}
