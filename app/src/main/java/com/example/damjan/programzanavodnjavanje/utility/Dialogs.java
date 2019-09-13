package com.example.damjan.programzanavodnjavanje.utility;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;

import com.example.damjan.programzanavodnjavanje.ActivityHelper.ISetValveData;
import com.example.damjan.programzanavodnjavanje.R;

import java.util.Calendar;

public final class Dialogs {

    private Dialogs()
    {

    }

    public final static String POSITION = "Position";
    public final static String FUNCTION = "Function";

    public final static int FUNCTION_UPDATE_NUMBER = 1;
    public final static int FUNCTION_UPDATE_PERCENTAGE = 2;


    public static class TimePicker extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener
    {
        private int m_pos;          //the position of the ViewHolder that called this fragment
        private ISetValveData m_comm;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            m_pos = getArguments().getInt(POSITION);
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            m_comm = (ISetValveData)context;
        }

        public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
            m_comm.setTime((byte)hourOfDay, (byte)minute, m_pos);
        }
    }

    public static class MinutePicker extends DialogFragment {
        private ISetValveData m_comm;
        private EditText m_minuteText;
        private int m_pos;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            m_pos = getArguments().getInt(POSITION);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            m_minuteText = new EditText((Context) m_comm);
            m_minuteText.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setPositiveButton(R.string.positive_response, null);
            builder.setNegativeButton(R.string.negative_response, null);
            builder.setView(m_minuteText);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    if (TextUtils.isEmpty(m_minuteText.getText()))
                    {
                        m_minuteText.setError(getActivity().getString(R.string.enter_number_prompt));
                        return;
                    }
                    int minutes = Integer.parseInt(m_minuteText.getText().toString());
                    //m_comm.setTime((byte)-1, (byte)minutes, m_pos);
                    m_comm.setTimeCountdown(minutes, m_pos);
                    //Dismiss once everything is OK.
                    dialog.dismiss();
                });
            });
            return dialog;
        }
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            m_comm = (ISetValveData) context;
        }
    }

    public static class NumberPicker extends DialogFragment {
        private ISetValveData m_comm;
        private EditText m_numberText;
        private int m_pos;
        private int function;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            m_pos = getArguments().getInt(POSITION);
            function = getArguments().getInt(FUNCTION);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            m_numberText = new EditText((Context) m_comm);
            m_numberText.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setPositiveButton(R.string.positive_response, null);
            builder.setNegativeButton(R.string.negative_response, null);
            builder.setView(m_numberText);
            final AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    if (TextUtils.isEmpty(m_numberText.getText()))
                    {
                        m_numberText.setError(getActivity().getString(R.string.enter_number_prompt));
                        return;
                    }
                    int number = Integer.parseInt(m_numberText.getText().toString());
                    switch (function)
                    {
                        case FUNCTION_UPDATE_NUMBER:
                        {
                            m_comm.setValveNumber((byte) number, m_pos);
                            break;
                        }

                        case FUNCTION_UPDATE_PERCENTAGE:
                        {
                            m_comm.setGroupPercent(number, m_pos);
                            break;
                        }
                    }
                    //Dismiss once everything is OK.
                    dialog.dismiss();
                });
            });
            return dialog;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            m_comm = (ISetValveData) context;
        }
    }


    public static class NamePicker extends DialogFragment {
        private ISetValveData m_comm;
        private EditText m_nameText;
        private int m_pos;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            m_pos = getArguments().getInt(POSITION);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            m_nameText = new EditText((Context) m_comm);
            builder.setPositiveButton(R.string.positive_response, null);
            builder.setNegativeButton(R.string.negative_response, null);
            builder.setView(m_nameText);
            final AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    if (TextUtils.isEmpty(m_nameText.getText()))
                    {
                        m_nameText.setError(getActivity().getString(R.string.enter_number_prompt));
                        return;
                    }
                    if(m_pos != -1)
                        m_comm.setValveName(m_nameText.getText().toString(), m_pos);
                    else
                        m_comm.setActiveGroupName(m_nameText.getText().toString());
                    //Dismiss once everything is OK.
                    dialog.dismiss();
                });
            });
            return dialog;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            m_comm = (ISetValveData) context;
        }
    }

    public static class MyAlertDialog extends android.app.DialogFragment
    {
        public static final String MESSAGE_ID_KEY = "messageID";
        private int         m_messageID;
        private Activity    m_activity;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            m_activity = getActivity();
            m_messageID = getArguments().getInt(MESSAGE_ID_KEY, R.string.default_alert_message);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(m_activity);
            builder.setMessage(m_messageID);
            builder.setNeutralButton(android.R.string.ok, null);
            return builder.create();
        }
    }
}
