package com.bit6.samples.authfb;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.ui.RtcMediaView;

public class CallActivity extends Activity implements RtcDialog.StateListener {
    private RtcMediaView mediaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Bit6 bit6 = Bit6.getInstance();
        CallClient callClient = bit6.getCallClient();
        final RtcDialog dialog = callClient.getDialogFromIntent(getIntent());
        dialog.addStateListener(this);
        mediaView = (RtcMediaView) findViewById(R.id.media_view);
        mediaView.init(bit6, this);

        ImageButton disconnect = (ImageButton) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hangup();
            }
        });
    }

    @Override
    public void onStateChanged(RtcDialog d, int state) {
        if (state == RtcDialog.END) {
            finish();
        }
    }
}
