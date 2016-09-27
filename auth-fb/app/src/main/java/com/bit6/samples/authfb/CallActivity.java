package com.bit6.samples.authfb;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.ui.RtcMediaView;

public class CallActivity extends Activity implements CallClient.StateListener, View.OnClickListener {

    private static final int REQ_PERMISSION_WEBRTC = 100;
    private RtcDialog dialog;
    private CallClient callClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        callClient = Bit6.getInstance().getCallClient();

        if (RtcMediaView.checkPermissions(this, REQ_PERMISSION_WEBRTC)) {
            // Have all the permissions - initialize InCallView
            initInCallView();
        }

    }

    @Override
    public void onStateChanged(RtcDialog d) {
        if (d.getState() == RtcDialog.END) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_WEBRTC) {
            // Do we have sufficient permissions now?
            // Note the second argument is '0' which means that
            // the permissions will be checked only and not requested
            if (RtcMediaView.checkPermissions(this, 0)) {
                // Have all the permissions - initialize InCallView
                initInCallView();
            } else {
                // Permissions were not granted - cannot proceed with calling
                // Just close the InCall UI
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        dialog.hangup();
    }

    private void initInCallView() {
        dialog = callClient.getDialogFromIntent(getIntent());
        callClient.addStateListener(this);

        ImageButton disconnect = (ImageButton) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
    }
}
