package net.givelives.givelives.utility;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.givelives.givelives.R;

/**
 * Created by MainAcc on 3/12/2017.
 */

public class AboutUsFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View view=inflater.inflate(R.layout.nav_about_us, null);
        TextView textview=(TextView)view.findViewById(R.id.textmsg);
        textview.setText(formatDonationInstructionDetails(getResources()));

        // make the link inside the message clickable
        textview.setMovementMethod(LinkMovementMethod.getInstance());

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.about_us_title))
                .setView(view)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Helper method to format the text nicely.
     */
    private static Spanned formatDonationInstructionDetails(Resources res) {
        return Html.fromHtml(res.getString(R.string.about_us_msg));
    }
}
