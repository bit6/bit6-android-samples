package com.bit6.samples.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.ui.RtcMediaView;
import com.bit6.ui.Contact;
import com.bit6.ui.InCallView;
import com.bit6.ui.IncomingCallReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CallActivity extends AppCompatActivity implements CallClient.StateListener, View.OnClickListener {

    private static final int
            REQ_PERMISSION_WEBRTC = 100;

    private Bit6 bit6;
    private MyContactSource cs;
    private boolean useVideo;
    private ImageButton addParticipantButton;
    private boolean iconsVisible = true, isConnected;

    // Receiver which will handle incoming call notifications
    // only when there are existing ongoing calls
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RtcDialog dialog = bit6.getCallClient().getDialogFromIntent(intent);
            showIncomingCallDialog(dialog);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        getWindow().setFlags(flags, flags);

        setContentView(R.layout.activity_call);

        InCallView inCallView = (InCallView) findViewById(R.id.incall_view);
        inCallView.setOnClickListener(this);

        bit6 = Bit6.getInstance();
        CallClient callClient = bit6.getCallClient();
        // App-specific ContactSource
        cs = ((App) getApplication()).getContactSource();

        // A button to add a new participant to the call
        addParticipantButton = (ImageButton) findViewById(R.id.add_person);
        addParticipantButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAddParticipantDialog();
            }
        });

        // Let IncomingCallReceiver to know that there is an already ongoing call
        IncomingCallReceiver.setAlreadyInCall(true);
        // Register incoming call receiver
        registerReceiver(receiver, new IntentFilter(getPackageName() + Bit6.INCOMING_CALL_INTENT_SUFFIX));

        // This is mostly for SDK 23
        // Check if the user has granted sufficient permissions for WebRTC calling
        // If not - they will be requested, and the response will be provided in
        // onRequestPermissionsResult()
        if (RtcMediaView.checkPermissions(this, REQ_PERMISSION_WEBRTC)) {
            // Have all the permissions - initialize InCallView
            initInCallView();
        }

        if (callClient != null) {
            callClient.addStateListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.incall_view) {
            iconsVisible = !iconsVisible;
        }

        if (!isConnected) {
            return;
        }
        if (iconsVisible) {
            addParticipantButton.setVisibility(View.VISIBLE);
        } else {
            addParticipantButton.setVisibility(View.GONE);
        }

    }

    private void initInCallView() {
        // InCallView UI
        InCallView inCallView = (InCallView) findViewById(R.id.incall_view);
        // Initiate inCallView before using it
        inCallView.init(cs);
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
    public void onStateChanged(RtcDialog d) {
        // A call answered
        if (d.getState() == RtcDialog.ANSWER) {
            useVideo = d.hasVideo();
            isConnected = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (iconsVisible) {
                        addParticipantButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        // A call finished
        else if (d.getState() == RtcDialog.END || d.getState() == RtcDialog.MISSED) {
            // Was this the last ongoing call?
            if (bit6.getCallClient().getRtcDialogs().size() == 0) {
                // Yes, end this activity
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Let IncomingCallReceiver to know that ongoing call is ended
        IncomingCallReceiver.setAlreadyInCall(false);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    // Show a list of contacts that is used to add a participant to the call
    private void showAddParticipantDialog() {

        // Identities already participating in the call
        HashMap<String, String> participants = new HashMap<>();
        List<RtcDialog> calls = bit6.getCallClient().getRtcDialogs();
        for (RtcDialog c : calls) {
            String id = c.getOther();
            participants.put(id, id);
        }

        // Contacts that can be added to the call
        final ArrayList<Contact> contacts = new ArrayList<>();
        // All contacts data
        ArrayList<Contact> data = cs.getContactsAsArrayList();
        // Keep only contacts that are not participating in the call
        for (Contact c : data) {
            String id = c.getId();
            if (!participants.containsKey(id) && !c.getId().startsWith("grp:")) {
                contacts.add(c);
            }
        }

        // Dialog list items
        CharSequence[] items = new CharSequence[contacts.size()];
        for (int i = 0, n = contacts.size(); i < n; i++) {
            items[i] = contacts.get(i).getDisplayName();
        }

        // Create AlertDialog of list of items
        // http://developer.android.com/guide/topics/ui/dialogs.html#AddingAList
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.call_add_participant_dialog_title)
                .setCancelable(true)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int idx) {
                        // Contact we are calling to
                        Contact c = contacts.get(idx);
                        // Start an outgoing call
                        bit6.getCallClient().startCall(Address.parse(c.getId()), useVideo, RtcDialog.MODE_P2P);
                    }
                });

        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // Show dialog if incoming call arrives
    private void showIncomingCallDialog(final RtcDialog d) {
        // Caller identity
        String other = d.getOther();
        // Try to find a Contact info for this caller
        Contact c = cs.get(other);
        // Display name for the caller
        String displayName = c != null ? c.getDisplayName() : other;

        String title = String.format(getString(R.string.incoming_call_text), displayName);

        // Show IncomingCall dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setPositiveButton(R.string.incoming_call_answer, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        d.answer(useVideo);
                    }
                })
                .setNegativeButton(R.string.incoming_call_reject, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        d.hangup();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (bit6 != null && bit6.getCallClient() != null) {
            bit6.getCallClient().hangupAll();
        }
        super.onBackPressed();
    }
}
