package com.bit6.samples.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.db.Contract.Conversations;
import com.bit6.sdk.util.Utils;
import com.bit6.ui.ContactSource;
import com.bit6.ui.ConversationList;

import org.json.JSONObject;

public class ChatsActivity extends AppCompatActivity {

    private Bit6 bit6;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bit6 = Bit6.getInstance();

        // Init ConversationList UI component
        ContactSource cs = ((App) getApplication()).getContactSource();
        ConversationList conversationList = (ConversationList) findViewById(R.id.conversation_list);
        conversationList.init(cs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chats_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pstn_call:
                showPstnCallDialog();
                break;
            case R.id.menu_direct_chat:
                showComposeDialog();
                break;
            case R.id.menu_create_group:
                showCreateGroupDialog();
                break;
            case R.id.menu_logout:
                bit6.getSessionClient().logout();
                // Go back to login screen
                Intent intent = new Intent(ChatsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChatActivity(Uri uri) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    // Show UI for making a phone call
    private void showPstnCallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_pstn_call, null);
        final EditText phoneNumber = (EditText) layout.findViewById(R.id.phone_number);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.pstn_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                int len = phone.length();
                // Is phone number valid?
                if (len > 6 && len < 14 && phone.charAt(0) == '+') {
                    bit6.getCallClient().startPhoneCall(phone);
                    dialog.dismiss();
                } else {
                    Toast.makeText(ChatsActivity.this,
                            R.string.error_invalid_phone_number,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void showComposeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_compose, null);
        final EditText dest = (EditText) layout.findViewById(R.id.dest);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.direct_chat_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String destStr = dest.getText().toString().trim();
                if (!TextUtils.isEmpty(destStr)) {
                    final Address other;

                    if (destStr.indexOf(':') > 0) {
                        other = Address.parse(destStr);
                    } else {
                        other = Address.fromParts(Address.KIND_USERNAME, destStr);
                    }
                    openChat(other);

                    dialog.dismiss();
                }
            }
        });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_edit_group, null);
        final EditText title = (EditText) layout.findViewById(R.id.group_title);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.create_group_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // The user may or may not provide a groupId to create
                // For these groups created from the UI it would make sense
                // to send null to server as groupId and let it create
                // a new group with unique id.

                // App specific group meta info
                String titleStr = title.getText().toString().trim();
                JSONObject meta = new JSONObject();
                Utils.jsonPut(meta, "title", titleStr);

                bit6.createGroup(null, null, meta, new ResultHandler() {
                    @Override
                    public void onResult(boolean success, String msg) {
                        if (!success) {
                            Log.e("Create group: ", msg);
                        }
                    }
                });

                dialog.dismiss();

            }
        });
    }

    private void openChat(Address other) {
        bit6.getMessageClient().addConversation(other);
        Uri uri = Uri.withAppendedPath(Conversations.CONTENT_URI_BY_IDENTITY, other.toString());
        showChatActivity(uri);
    }

}
