package com.example.damjan.programzanavodnjavanje.data.file;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class PrivateFileOperations
{
    private final Activity m_context;
    private final String                    m_saveFileName;
    private final ArrayList<IFileListener>  m_listeners;

    PrivateFileOperations(@NonNull String saveFileName, Activity activity, @Nullable IFileListener[] listeners) throws FileNotFoundException
    {
        m_context       = activity;
        m_saveFileName  = saveFileName;
        m_listeners = new ArrayList<>();

        if(listeners != null)
        {
            m_listeners.addAll(Arrays.asList(listeners));
        }
    }

    void writeAsync(byte[] data)
    {
        Thread writeToFileThread = new Thread(
                () ->
                {
                    boolean done = false;
                    try(FileOutputStream output = m_context.openFileOutput(m_saveFileName, Context.MODE_PRIVATE))
                    {
                        output.write(data);
                        done = true;
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }finally
                    {
                        for (IFileListener listener:m_listeners)
                            listener.doneWriting(done);
                    }
                }, "writeToFileThread"
        );
        writeToFileThread.start();
    }

    void readAsync()
    {
        Thread readEntireFileThread = new Thread(
                () ->
                {
                    ByteArrayOutputStream outputStream = null;
                    boolean done = false;
                    try(FileInputStream inputStream = m_context.openFileInput(m_saveFileName))
                    {
                        int availableBytes = inputStream.available();
                        if(availableBytes < 1) return;
                        byte[] inStream;
                        outputStream = new ByteArrayOutputStream(availableBytes);
                        while (availableBytes > 0)
                        {
                            inStream = new byte[availableBytes];
                            int value = inputStream.read(inStream);
                            if(value == -1)
                                break;
                            outputStream.write(inStream);

                            availableBytes = inputStream.available();
                        }
                        done = true;
                    } catch (IOException e)
                    {
                        Log.e("File IO: ", e.toString());
                    }finally
                    {
                        for (IFileListener listener:m_listeners)
                        {
                            listener.doneReading((outputStream == null) ? null : outputStream.toByteArray(), done);
                        }
                    }

                }, "readEntireFileThread"
        );
        readEntireFileThread.start();
    }
}
