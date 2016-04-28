package com.bit6.samples.demo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Group;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.db.Contract;
import com.bit6.sdk.util.Utils;
import com.bit6.ui.ContactSource;

import org.json.JSONObject;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    private Bit6 bit6;
    private String groupId;
    private Toolbar toolbar;
    private String metaTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        (findViewById(R.id.invite_member)).setOnClickListener(this);

        bit6 = Bit6.getInstance();
        Uri uri = getIntent().getData();
        init(uri);
    }

    private void init(Uri uri) {
        long _id = 0;
        // Data for the current group
        Cursor gc = getContentResolver().query(uri, null, null, null, null);

        if (gc.moveToFirst()) {
            _id = gc.getLong(gc.getColumnIndex(Contract.Groups._ID));
            groupId = gc.getString(gc.getColumnIndex(Contract.Groups.ID));
            // Get group meta information
            JSONObject meta = Group.getMetaAsJson(gc.getString(gc.getColumnIndex(Contract.Groups.META)));
            // Get existing group title

            if (meta != null) {
                metaTitle = meta.optString("title");
            }
            getSupportActionBar().setTitle(!TextUtils.isEmpty(metaTitle) ? metaTitle : "Group");
        }

        // Cursor for this group's members
        Cursor mc = getContentResolver().query(Contract.Members.CONTENT_URI, null,
                Contract.Members.GROUP_ID + " =?", new String[]{String.valueOf(_id)}, Contract.Members.ID + " ASC");

        ListView listview = (ListView) findViewById(R.id.group_members);
        ContactSource cs = ((App) getApplication()).getContactSource();
        MemberAdapter adapter = new MemberAdapter(this, mc, groupId, cs, true);
        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, Menu.NONE, R.string.group_menu_edit_title);
        menu.add(0, 2, Menu.NONE, R.string.group_menu_delete_group);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                showCreateGroupDialog();
                break;
            case 2:
                deleteGroup(groupId);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteGroup(String name) {

        bit6.deleteGroup(name, new ResultHandler() {
            @Override
            public void onResult(boolean success, String msg) {
                if (success) {
                    finish();
                } else {
                    Toast.makeText(GroupActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showInviteMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_invite_member, null);
        final EditText userName = (EditText) layout.findViewById(R.id.member_identity);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.group_invite_member_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Show the dialog
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String other = userName.getText().toString();
                if (other.trim().length() == 0) {
                    return;
                }
                if (other.indexOf(':') < 0) {
                    // If not URI scheme, assume username
                    other = "usr:" + other;
                }
                inviteUser(other);
                dialog.dismiss();
            }
        });
    }

    private void inviteUser(String userName) {
        bit6.inviteGroupMember(groupId, userName, Group.Permissions.ROLE_USER, new ResultHandler() {
            @Override
            public void onResult(boolean success, String msg) {
                if (!success) {
                    Log.e("Invite user", msg);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.invite_member:
                showInviteMemberDialog();
                break;
        }
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_edit_group, null);
        final EditText title = (EditText) layout.findViewById(R.id.group_title);
        title.setText(metaTitle);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.create_group_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // App specific group meta info
                final String titleStr = title.getText().toString().trim();
                JSONObject meta = new JSONObject();
                Utils.jsonPut(meta, "title", titleStr);
                bit6.updateGroup(groupId, null, meta, new ResultHandler() {
                    @Override
                    public void onResult(boolean success, String msg) {
                        if (success) {
                            metaTitle = titleStr;
                            getSupportActionBar().setTitle(metaTitle);
                        } else {
                            Toast.makeText(GroupActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
    }

}
