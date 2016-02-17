
package com.bit6.samples.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.db.Contract.Conversations;
import com.bit6.ui.ContactSource;
import com.bit6.ui.ConversationList;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatsActivity extends Activity {

    private Bit6 bit6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bit6 = Bit6.getInstance();

        // Logout button
        TextView mLogout = (TextView) findViewById(R.id.logout);
        mLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Logout and delete saved data
                bit6.getSessionClient().logout();
                // Go back to login screen
                Intent intent = new Intent(ChatsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Show current user name
        String txt = getString(R.string.logged_in, bit6.getSessionClient().getOwnIdentity()
                .toString());
        TextView tv = (TextView) findViewById(R.id.username);
        tv.setText(txt);

        // Init ConversationList UI component
        ContactSource cs = ((App)getApplication()).getContactSource();
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
            case R.id.menu_add_person:
                showComposeDialog(false);
                break;
            case R.id.menu_add_group:
                showComposeDialog(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Show Chat activity
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
     // Show the dialog
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                int len = phone != null ? phone.length() : 0;
                // Is phone number valid?
                if (len > 6 && len < 14 && phone.charAt(0) == '+') {
                    RtcDialog d = bit6.getCallClient().startPhoneCall(phone);
                    dialog.dismiss();
                } else {
                    Toast.makeText(ChatsActivity.this,
                            R.string.invalid_phone_number,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    // Show UI for making a phone call
    // TODO: let's separate dialogs for compose and creating a new group.
    // In group creation we just ask for a title, not groupId.
    private void showComposeDialog(final boolean isGroup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_compose, null);
        final EditText dest = (EditText) layout.findViewById(R.id.dest);
        final EditText title = (EditText) layout.findViewById(R.id.group_title);
        if(isGroup){
            title.setVisibility(View.VISIBLE);
        }

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.add_conversation_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Show the dialog
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String destStr = dest.getText().toString().trim();
                if (!TextUtils.isEmpty(destStr)) {
                    final Address other;
                    if (isGroup) {
                        // TODO: The user may or may not provide a groupId to create
                        // For these groups created from the UI it would make sense
                        // to send null to server as groupId and let it create
                        // a new group with unique id.
                        other = Address.fromParts(Address.KIND_GROUP, destStr);
                        // App specific group meta info
                        String titleStr = title.getText().toString().trim();
                        JSONObject meta = new JSONObject();
                        try {
                            meta.putOpt("title", titleStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        bit6.createGroup(destStr, null, meta, new ResultHandler() {
                            @Override
                            public void onResult(boolean success, String msg) {
                                if (!success) {
                                    Log.e("Create group: ", msg);
                                }
                            }
                        });
                    } else {
                        if (destStr.indexOf(':') > 0) {
                            other = Address.parse(destStr);
                        } else {
                            other = Address.fromParts(Address.KIND_USERNAME, destStr);
                        }
                        openChat(other);
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    private void openChat(Address other){
        bit6.getMessageClient().addConversation(other);
        Uri uri = Uri.withAppendedPath(Conversations.CONTENT_URI_BY_IDENTITY, other.toString());
        showChatActivity(uri);
    }

}
