
package com.bit6.samples.authdigits;

import android.app.Activity;
import android.os.Bundle;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.RtcDialog.StateListener;
import com.bit6.sdk.ui.RtcMediaView;
import com.bit6.ui.InCallView;

public class CallActivity extends Activity implements StateListener {

    private Bit6 bit6;
    private InCallView inCallView;
    private static final int REQ_PERMISSION_WEBRTC = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        bit6 = Bit6.getInstance();

        // This is mostly for SDK 23
        // Check if the user has granted sufficient permissions for WebRTC calling
        // If not - they will be requested, and the response will be provided in
        // onRequestPermissionsResult()
        if (RtcMediaView.checkPermissions(this, REQ_PERMISSION_WEBRTC)) {
            // Have all the permissions - initialize InCallView
            initInCallView();
        }
    }

    private void initInCallView() {
        // InCallView UI
        inCallView = (InCallView) findViewById(R.id.incall_view);
        // Initiate inCallView before using it
        inCallView.init(this, null);

        // Get RtcDialog from intent and add it to the UI
        RtcDialog d = bit6.getCallClient().getDialogFromIntent(getIntent());
        addCall(d);
    }

    @Override
    public void onStateChanged(RtcDialog d, int state) {
        if (state == RtcDialog.END) {
            // Was this the last ongoing call?
            if (bit6.getCallClient().getRtcDialogs().size() == 0) {
                // Yes, end this activity
                finish();
            }
        }
    }

    // Add a new call to the UI
    void addCall(RtcDialog d) {
        d.addStateListener(this);
        inCallView.addCall(d);
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
}
