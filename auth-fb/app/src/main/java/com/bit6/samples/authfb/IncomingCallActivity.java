
package com.bit6.samples.authfb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.NotificationClient;
import com.bit6.sdk.Ringer;
import com.bit6.sdk.RtcDialog;

import org.json.JSONObject;

public class IncomingCallActivity extends AppCompatActivity implements
        CallClient.StateListener, OnClickListener, NotificationClient.Listener {

    private RtcDialog dialog;
    private ImageButton answer, reject;
    private Ringer ringer;
    private Bit6 bit6;
    private CallClient callClient;
    private boolean answered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int flag = getIntent().getFlags();
        if ((flag & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        getWindow().setFlags(flags, flags);

        ringer = new Ringer(this);
        bit6 = Bit6.getInstance();
        callClient = bit6.getCallClient();
        callClient.addStateListener(this);
        dialog = callClient.getDialogFromIntent(getIntent());

        TextView message = (TextView) findViewById(R.id.message);
        message.setText(dialog.hasVideo() ? R.string.incoming_video_call : R.string.incoming_voice_call);

        String other = dialog.getOther();


        String callerName = other.substring(other.indexOf(":") + 1);


        TextView name = (TextView) findViewById(R.id.caller_name);
        name.setText(callerName);


        answer = (ImageButton) findViewById(R.id.answer);
        reject = (ImageButton) findViewById(R.id.reject);

        answer.setOnClickListener(this);
        reject.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ringer.playRinging();
    }

    @Override
    protected void onStop() {
        ringer.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (callClient != null) {
            callClient.removeStateListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == answer) {

            if (bit6.getNotificationClient().isConnected()) {
                onAnswerClicked();
            } else {
                answered = true;
            }
        } else if (v == reject) {
            ringer.stop();
            dialog.hangup();
            finish();
        }
    }

    private void onAnswerClicked() {
        ringer.stop();
        dialog.answer(dialog.hasVideo());
        finish();
    }

    @Override
    public void onStateChanged(RtcDialog d) {
        if (d.getState() == RtcDialog.END) {
            finish();
        }
    }

    @Override
    public void onTypingReceived(String from) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNotificationReceived(String from, String type, JSONObject data) {
        // TODO Auto-generated method stub

    }

    @Override

    public void onConnectedChanged(boolean isConnected) {
        if (isConnected && answered) {
            onAnswerClicked();
        }
    }

    @Override
    public void onBackPressed() {
        dialog.hangup();
        super.onBackPressed();
    }

}
