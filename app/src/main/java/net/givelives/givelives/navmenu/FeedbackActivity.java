package net.givelives.givelives.navmenu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.givelives.givelives.R;

/**
 * Created by MainAcc on 3/12/2017.
 */

public class FeedbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_feedback_form);

        ((Button) findViewById(R.id.btnOK)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String giveLivesEmailAddress = getString(R.string.giveLivesEmailAddress);
                String sub = ((EditText)findViewById(R.id.txtSubject)).getText().toString();
                String mess = ((EditText)findViewById(R.id.txtMessage)).getText().toString();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL,new String[]{giveLivesEmailAddress});
                intent.putExtra(Intent.EXTRA_SUBJECT, sub);
                intent.putExtra(Intent.EXTRA_TEXT, mess);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }
}