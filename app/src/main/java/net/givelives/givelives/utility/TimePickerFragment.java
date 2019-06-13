package net.givelives.givelives.utility;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import net.givelives.givelives.blood.AddBloodRequestActivity;

import java.util.Calendar;

/**
 * Created by MainAcc on 2/11/2017.
 */

public class TimePickerFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current transfusionTime as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), (AddBloodRequestActivity)getActivity(), hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


}
