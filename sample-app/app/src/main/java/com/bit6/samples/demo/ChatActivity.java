package com.bit6.samples.demo;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.Group;
import com.bit6.sdk.NotificationClient;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.db.Contract;
import com.bit6.sdk.util.Utils;
import com.bit6.ui.ActionItem;
import com.bit6.ui.ComposeView;
import com.bit6.ui.Contact;
import com.bit6.ui.ContactSource;
import com.bit6.ui.ConversationView;
import com.bit6.ui.IncomingMessageReceiver;
import com.bit6.ui.MediaUtils;

import org.json.JSONObject;


public class ChatActivity extends AppCompatActivity implements NotificationClient.Listener {

    private Bit6 bit6;
    private Address other;
    private ComposeView composeView;
    private ConversationView conversationView;
    private boolean isGroupChat;
    private Toolbar toolbar;
    // App-specific ContactSource
    private ContactSource cs;
    private Settings settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        conversationView = (ConversationView) findViewById(R.id.conversation);
//        conversationView.setFilter(Contract.Messages.TYPE_MASK, Contract.Messages.IS, Contract.Messages.TYPE_CALL);
        composeView = conversationView.getComposeView();
        // Bit6 instance
        bit6 = Bit6.getInstance();
        settings = new Settings(this);
        // Listen to 'typing' notifications
        bit6.getNotificationClient().addListener(this);
        cs = ((App) getApplication()).getContactSource();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Response from the MediaUtils when adding message attachments
        if (Intent.ACTION_ATTACH_DATA.equals(intent.getAction())) {
            composeView.setAttachment(intent);
        }
        // Otherwise it should be VIEW intent to show a conversion
        else {
            Uri uri = intent.getData();
            showConversation(uri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (other != null) {
            // Set chat id to IncomingMessageReceiver to avoid showing notification from this conversation
            IncomingMessageReceiver.setConversationId(other.toString());
            showConversationTitle();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Reset chat id if screen is not visible
        IncomingMessageReceiver.setConversationId(null);
    }

    @Override
    protected void onDestroy() {
        // Remove listeners
        bit6.getNotificationClient().removeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_video_call:
                startCall(true);
                break;
            case R.id.menu_voice_call:
                startCall(false);
                break;
            case R.id.menu_open_group:
                openGroupView();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGroupView() {
        if (other == null) {
            return;
        }

        long grp_id = 0;
        Cursor groupsCursor = getContentResolver().query(Contract.Groups.CONTENT_URI,
                null, Contract.Groups.ID + " =?", new String[]{other.getValue()}, null);

        if (groupsCursor != null && groupsCursor.moveToFirst()) {
            grp_id = groupsCursor.getLong(groupsCursor.getColumnIndex(Contract.Groups._ID));
            groupsCursor.close();
        }

        Uri uri = Uri.withAppendedPath(Contract.Groups.CONTENT_URI, String.valueOf(grp_id));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (Utils.isIntentResolvable(this, intent)) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        super.onMenuOpened(featureId, menu);
        if (!isGroupChat && menu != null) {
            menu.removeItem(R.id.menu_open_group);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get response from the MediaUtils when adding message attachments
        Intent intent = MediaUtils.getAttachmentFromActivityResult(this, requestCode, resultCode, data);
        if (intent != null) {
            composeView.setAttachment(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MediaUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    @Override
    public void onTypingReceived(String from) {

        if (other.toString().equalsIgnoreCase(from)) {
            String info = other.getValue() + " is typing";
            toolbar.setSubtitle(info);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setSubtitle("");
                }
            }, 9000);

        }
    }

    @Override
    public void onNotificationReceived(String from, String type, JSONObject data) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectedChanged(boolean isConnected) {
        // TODO Auto-generated method stub
    }

    private void showConversation(Uri uri) {
        // Initiate conversationView with conversation uri
        conversationView.init(uri, cs);
        // When viewed, the messages will be marked as read automatically
        conversationView.getMessageList().setMarkAsReadOnView(true);
        // Conversation address
        other = Address.parse(conversationView.getConversationId());
        // By default this is a non-group chat
        isGroupChat = false;

        // Action items available in the ComposeView
        ActionItem[] items = new ActionItem[]{
                new ActionItem(getString(R.string.compose_choose_photo), R.drawable.ic_action_picture) {
                    @Override
                    public void run() {
                        MediaUtils.showImageChooser(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.compose_take_photo), R.drawable.ic_action_camera) {
                    @Override
                    public void run() {
                        MediaUtils.showPhotoCamera(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.compose_record_video), R.drawable.ic_action_play) {
                    @Override
                    public void run() {
                        MediaUtils.showVideoRecorder(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.compose_share_location), R.drawable.ic_action_share) {
                    @Override
                    public void run() {
                        // Response will be returned into ChatActivity.onNewIntent()
                        MediaUtils.requestMyLocation(ChatActivity.this);
                    }
                }
        };

        composeView.setActionItems(items);
    }

    private void showConversationTitle() {
        String title = "Unknown";
        // Should be able to easily test if this is a Group by
        // looking at other - it will be grp:abc123
        if (other.getKind().startsWith(Address.KIND_GROUP)) {
            // Get corresponding Group
            Cursor c = getContentResolver().query(
                    Contract.Groups.CONTENT_URI, null, Contract.Groups.ID + "=?",
                    new String[]{
                            other.getValue()
                    }, null);

            // Do we have a Group for this conversation?
            if (c != null && c.getCount() != 0) {
                c.moveToFirst();
                // Get Group meta
                String metaStr = c.getString(c.getColumnIndex(Contract.Groups.META));
                JSONObject meta = Group.getMetaAsJson(metaStr);
                // Get the title for this conversation - app-specific group meta info
                title = meta != null ? meta.optString("title") : null;
                // No title in meta? - try to get group members and show their names as title
                if (TextUtils.isEmpty(title)) {
                    long _id = c.getLong(c.getColumnIndex(Contract.Groups._ID));
                    title = getGroupTitleFromMembers(_id);
                }
                if (TextUtils.isEmpty(title)) title = "Unnamed Group";
                isGroupChat = true;
                c.close();
            }
        } else {
            Contact c = cs.get(other.toString());
            title = c != null ? c.getDisplayName() : other.getValue();
        }
        getSupportActionBar().setTitle(title);
    }

    // Start voice or video call
    private void startCall(boolean isVideo) {
        String mediaMode = settings.getMediaMode() == Settings.MODE_P2P ? RtcDialog.MODE_P2P : RtcDialog.MODE_MIX;
        bit6.getCallClient().startCall(other, isVideo, mediaMode);
    }

    private String getGroupTitleFromMembers(long _id) {
        String title = null;
        Cursor members = getContentResolver().query(Contract.Members.CONTENT_URI, null,
                Contract.Members.GROUP_ID + " =?", new String[]{String.valueOf(_id)}, null);
        if (members != null && members.moveToFirst()) {
            while (!members.isAfterLast()) {
                String memberId = members.getString(members.getColumnIndex(Contract.Members.ID));
                Contact c= cs.get(memberId);
                if(c == null){
                    continue;
                }
                String name = c.getDisplayName();
                if (title == null) {
                    title = name;
                } else {
                    title = title + ", " + name;
                }
                members.moveToNext();
            }
            members.close();
        }
        return title;
    }

}
