package com.example.damjan.programzanavodnjavanje.data.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.example.damjan.programzanavodnjavanje.ConsoleActivity;
import com.example.damjan.programzanavodnjavanje.data.Error;
import com.example.damjan.programzanavodnjavanje.data.MyCalendar;
import com.example.damjan.programzanavodnjavanje.data.ValveGroup;
import com.example.damjan.programzanavodnjavanje.data.ValveOptionsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ArduinoComms extends Thread
{

    private static final ArduinoComms               ONLY_INSTANCE = new ArduinoComms();
    private final static String                     MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final BlockingQueue<Runnable>    TASK_LIST = new LinkedBlockingQueue<>();
    private static InputStream                      inputStream;
    private static OutputStream                     outputStream;
    private static ArrayList<IBluetoothComms>       comms = new ArrayList<>();
    private static BluetoothSocket                  socket;

    static
    {
        ONLY_INSTANCE.start();
    }
    private ArduinoComms()
    {
        super("BluetoothCommThread");
    }

    public static void connect(final BluetoothDevice device)
    {
        connect(device, 3);
    }

    public static void connect(final BluetoothDevice device, int connectRetryCount)
    {

        TASK_LIST.add(() ->
        {
            if (socket != null && socket.isConnected() && socket.getRemoteDevice().equals(device))
            {
                return;
            }

            disconnectInternal();

            UUID uuid = UUID.fromString(MY_UUID);
            try
            {
                socket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
                notifyConnectionFailed();
                return;
            }
            int retryCount = 0;
            do
            {
                try
                {
                    socket.connect();
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                } catch (IOException e)
                {
                    ConsoleActivity.log(e.toString() + '\n');
                    retryCount++;
                }
            } while (retryCount < connectRetryCount && !socket.isConnected());
            if (socket.isConnected())
            {
                notifyConnected();
            } else
            {
                //failed to connect...
                notifyConnectionFailed();
            }

/*
			Message msg = new Message();
			try
			{
				msg.read(inputStream);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
*/
        });
    }

    //we add the task to notify disconnected because
    //disconnectInternal is used internally and we don't want to
    //notify listeners from internal calls
    public static void disconnect()
    {
        TASK_LIST.add(ArduinoComms::disconnectInternal);
        TASK_LIST.add(ArduinoComms::notifyDisconnected);
        //TODO if waiting at socket.connect from connect() notify the tread
    }

    private static void disconnectInternal()
    {
        try
        {
            if (socket == null)
            {
                return;
            }
            socket.close();
            socket = null;
            inputStream = null;
            outputStream = null;
        } catch (IOException e)
        {
            ConsoleActivity.log(e.toString());
        }
    }

    public static void registerListener(@NonNull IBluetoothComms comm)
    {
        if (comms.contains(comm))
        {
            return;
        }
        comms.add(comm);
        if (socket != null && socket.isConnected())
        {
            comm.connected();//notify the listener that we are already connected.
        }
    }

    public static void unregisterListener(IBluetoothComms comm)
    {
        comms.remove(comm);
    }

    public static void getTemp()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msg = new Message(Message.Type.REQUEST, Message.Action.TEMPERATURE);
                msg.write(outputStream);
                msg.read(inputStream);

                notifySetTemperature(Integer.toString(msg.at(0)));
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void getTempFloat()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msg = new Message(Message.Type.REQUEST, Message.Action.TEMPERATURE_FLOAT);
                msg.write(outputStream);
                msg.read(inputStream);

                notifySetTemperature(new String(msg.getData()));//we are receiving float as a string
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void getValves()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msg = new Message(Message.Type.REQUEST, Message.Action.VALVE);
                msg.write(outputStream);
                msg.read(inputStream);
                byte valveCount = msg.at(0);//this is silly, just create a utility function, do the same for temp, while you're at it
                ValveGroup arduinoValves = new ValveGroup("arduinoValves");
                for (int i = 0; i < valveCount; i++)
                {
                    msg.read(inputStream);
                    arduinoValves.add(new ValveOptionsData(msg));
                }
                notifySetValves(arduinoValves.toArray(new ValveOptionsData[0]));
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void sendValves(final ValveGroup valves)
    {
        TASK_LIST.add(() ->
        {
            try
            {

                Message msg = new Message(Message.Type.COMMAND, Message.Action.VALVE, (byte) 1);
                msg.set(0, (byte) valves.size());
                msg.write(outputStream);

                for (ValveOptionsData data : valves)
                {
                    msg.read(inputStream);//wait for arduino to be ready to receive
                    if (msg.getType() != Message.Type.INFO || msg.getInfo() != Message.Info.READY_TO_RECEIVE)
                        throw new RuntimeException("[ArduinoComms] Received message is not of expected Message.Type " + msg.toString());
                    data.toMessage().write(outputStream);
                }
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void sendTime(final MyCalendar date)
    {
        TASK_LIST.add(() ->
        {
            try
            {
                date.toMessage().write(outputStream);
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });

    }

    public static void getTime()
    {
        TASK_LIST.add(() ->
        {
            try
            {

                Message msg = new Message(Message.Type.REQUEST, Message.Action.TIME);

                msg.write(outputStream);

                MyCalendar date = new MyCalendar();
                date.setFirstDayOfWeek(Calendar.SUNDAY);

                msg.read(inputStream);
                date.fromMessage(msg);

                notifySetTime(date);
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void getHBridgePin()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msg = new Message(Message.Type.REQUEST, Message.Action.H_BRIDGE_PIN);
                msg.write(outputStream);

                msg.read(inputStream);
                byte[] hbridgePin = msg.getData();
                ConsoleActivity.log("Hbridge: " + hbridgePin[0] + ", " + hbridgePin[1]);
            } catch (IOException e)
            {
                ConsoleActivity.log(e.toString());
            }
        });
    }

    public static void getErrors()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msg = new Message(Message.Type.REQUEST, Message.Action.ERROR);
                msg.write(outputStream);
                ArrayList<Error> list = new ArrayList<>();
                do
                {
                    msg.read(inputStream);
                    list.add(new Error(msg));
                }while(msg.getType() != Message.Type.INFO);
                notifyError(list);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        });
    }

    public static void getSleepTimes()
    {
        TASK_LIST.add(() ->
        {
            try
            {
                Message msgShort = new Message(Message.Type.REQUEST, Message.Action.SLEEP_TIME_SHORT);
                msgShort.write(outputStream);
                msgShort.read(inputStream);
                MyCalendar sleepTimeShortCal = new MyCalendar();
                sleepTimeShortCal.fromMessage(msgShort);

                Message msgLong = new Message(Message.Type.REQUEST, Message.Action.SLEEP_TIME_LONG);
                msgLong.write(outputStream);
                msgLong.read(inputStream);
                MyCalendar sleepTimeLongCal = new MyCalendar();
                sleepTimeLongCal.fromMessage(msgLong);

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        });
    }

    private static void notifyConnected()
    {
        for (IBluetoothComms comm : comms)
        {
            comm.connected();
        }
    }

    private static void notifyConnectionFailed()
    {
        for (IBluetoothComms comm : comms)
        {
            comm.connectionFailed();
        }
    }

    private static void notifyDisconnected()
    {
        for (IBluetoothComms comm : comms)
        {
            comm.disconnected();
        }
    }

    private static void notifySetTemperature(String temperature)
    {
        for (IBluetoothComms comm : comms)
        {
            comm.setTemperature(temperature);
        }
    }

    private static void notifySetTime(Calendar time)
    {
        for (IBluetoothComms comm : comms)
        {
            comm.setTime(time);
        }
    }

    private static void notifySetValves(ValveOptionsData[] valves)
    {
        for (IBluetoothComms comm : comms)
        {
            comm.setValves(valves);//TODO:  should we modify the main Valves Array?
        }
    }

    private static void notifyError(ArrayList<Error> errors)
    {
        for (IBluetoothComms comm : comms)
        {
            comm.setErrors(errors.toArray(new Error[0]));//comm.error(errors);
        }
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                TASK_LIST.take().run();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
