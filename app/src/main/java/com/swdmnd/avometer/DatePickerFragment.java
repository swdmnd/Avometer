package com.swdmnd.avometer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.DateFormatSymbols;

/**
 * Created by Arief on 9/11/2016.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    int currentYear;
    int currentMonth;
    int currentDate;

    public DatePickerFragment(){

    }

    public interface CallbackListener{
        void onReturnValue(int year, int month, int date);
    }

    CallbackListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mCallback =(CallbackListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement BluetoothSearchDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle args){
        super.onCreate(args);
        if(getArguments()!=null){
            currentYear = getArguments().getInt("year");
            currentMonth = getArguments().getInt("month");
            currentDate = getArguments().getInt("date");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, currentYear, currentMonth, currentDate);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    // Do Stuff
                    dialog.dismiss();
                }
            }
        });

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Do Stuff
                    DatePicker datePicker = ((DatePickerDialog)getDialog()).getDatePicker();
                    mCallback.onReturnValue(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        // Sudah ditangani di onDismiss()
    }
}