package net.givelives.givelives.utility;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import net.givelives.givelives.blood.AddBloodRequestActivity;

import java.util.Calendar;

/**
 * Created by MainAcc on 2/11/2017.
 */

public class DatePickerFragment extends DialogFragment
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current transfusionDate as the default transfusionDate in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), (AddBloodRequestActivity)getActivity(), year, month, day);
    }


}
