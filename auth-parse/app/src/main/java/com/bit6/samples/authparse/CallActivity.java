
package com.bit6.samples.authparse;

import android.app.Activity;
import android.os.Bundle;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.RtcDialog.StateListener;
import com.bit6.ui.InCallView;

public class CallActivity extends Activity implements StateListener {

    private Bit6 bit6;
    private InCallView inCallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        bit6 = Bit6.getInstance();

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
}
