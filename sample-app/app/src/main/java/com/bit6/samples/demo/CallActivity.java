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
import android.widget.ImageButton;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.RtcDialog.StateListener;
import com.bit6.sdk.ui.RtcMediaView;
import com.bit6.ui.Contact;
import com.bit6.ui.InCallView;
import com.bit6.ui.IncomingCallReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CallActivity extends AppCompatActivity implements StateListener {

    private static final int
            REQ_PERMISSION_WEBRTC = 100;

    private Bit6 bit6;
    private MyContactSource cs;
    private InCallView inCallView;
    private boolean useVideo;
    private ImageButton addParticipantButton;

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
        setContentView(R.layout.activity_call);

        bit6 = Bit6.getInstance();
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
    }

    private void initInCallView() {
        // InCallView UI
        inCallView = (InCallView) findViewById(R.id.incall_view);
        // Initiate inCallView before using it
        inCallView.init(this, cs);

        // Get RtcDialog from intent and add it to the UI
        RtcDialog d = bit6.getCallClient().getDialogFromIntent(getIntent());
        addCall(d);
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
    public void onStateChanged(RtcDialog d, int state) {
        // A call answered
        if (state == RtcDialog.ANSWER) {
            useVideo = d.hasVideo();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addParticipantButton.setVisibility(View.VISIBLE);
                }
            });
        }
        // A call finished
        else if (state == RtcDialog.END) {
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

    // Add a new call to the UI
    void addCall(RtcDialog d) {
        d.addStateListener(this);
        if (inCallView != null) {
            inCallView.addCall(d);
        }
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
            if (!participants.containsKey(id)) {
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
                        RtcDialog d = bit6.getCallClient().startCall(Address.parse(c.getId()), useVideo);
                        // Add to UI
                        addCall(d);
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
                        // Add to UI
                        addCall(d);
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
