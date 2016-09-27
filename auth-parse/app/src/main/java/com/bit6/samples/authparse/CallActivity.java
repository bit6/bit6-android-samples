
package com.bit6.samples.authparse;

import android.app.Activity;
import android.os.Bundle;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.ui.RtcMediaView;
import com.bit6.ui.InCallView;

public class CallActivity extends Activity implements CallClient.StateListener {

    private CallClient callClient;
    private static final int REQ_PERMISSION_WEBRTC = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        callClient = Bit6.getInstance().getCallClient();
        callClient.addStateListener(this);

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
        InCallView inCallView = (InCallView) findViewById(R.id.incall_view);
        // Initiate inCallView before using it
        inCallView.init(null);
    }

    @Override
    public void onStateChanged(RtcDialog d) {
        if (d.getState() == RtcDialog.END) {
            // Was this the last ongoing call?
            if (callClient.getRtcDialogs().size() == 0) {
                // Yes, end this activity
                finish();
            }
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
}
