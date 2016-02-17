
package com.bit6.samples.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.Group;
import com.bit6.sdk.NotificationClient;
import com.bit6.sdk.db.Contract;
import com.bit6.ui.ActionItem;
import com.bit6.ui.ComposeView;
import com.bit6.ui.ContactSource;
import com.bit6.ui.ConversationView;
import com.bit6.ui.IncomingMessageReceiver;
import com.bit6.ui.MediaUtils;

import org.json.JSONObject;


public class ChatActivity extends Activity implements NotificationClient.Listener {

    private Bit6 bit6;
    private Address other;
    private ComposeView composeView;
    private ActionBar actionBar;
    private ConversationView conversationView;
    private boolean isGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        actionBar = getActionBar();
        conversationView = (ConversationView) findViewById(R.id.conversation);
        composeView = conversationView.getComposeView();
        // Bit6 instance
        bit6 = Bit6.getInstance();
        // Listen to 'typing' notifications
        bit6.getNotificationClient().addListener(this);
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
        inflater.inflate(R.menu.conversation_options_menu, menu);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
            actionBar.setSubtitle(info);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    actionBar.setSubtitle(other.getValue());
                }
            }, 2000);

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
        // App-specific ContactSource
        ContactSource cs = ((App)getApplication()).getContactSource();
        // Initiate conversationView with conversation uri
        conversationView.init(uri, cs);
        // When viewed, the messages will be marked as read automatically
        conversationView.getMessageList().setMarkAsReadOnView(true);
        // Conversation address
        other = Address.parse(conversationView.getConversationId());
        // By default this a non-group chat
        isGroupChat = false;
        // Conversation title. By default - remote address
        String title = other != null ? other.getValue() : "Unknown";

        // Should be able to easily test if this is a Group by
        // looking at other - it will be grp:abc123
        if (other != null && other.toString().startsWith(Address.KIND_GROUP)) {
            // Get corresponding Group
            Cursor c = getContentResolver().query(
                    Contract.Groups.CONTENT_URI, null, Contract.Groups.ID + "=?",
                    new String[]{
                            other.getValue()
                    }, null);

            // Do we have a Group for this conversation?
            if (c.getCount() != 0) {
                c.moveToFirst();
                // Get Group meta
                String metaStr = c.getString(c.getColumnIndex(Contract.Groups.META));
                JSONObject meta = Group.getMetaAsJson(metaStr);
                // Get the title for this conversation - app-specific group meta info
                title = meta != null ? meta.optString("title") : null;
                if (title == null) title = "Unnamed Group";
                isGroupChat = true;
            }
        }
        actionBar.setSubtitle(title);

        // Action items available in the ComposeView
        ActionItem[] items = new ActionItem[]{
                new ActionItem(getString(R.string.choose_photo), R.drawable.ic_action_picture) {
                    @Override
                    public void run() {
                        MediaUtils.showImageChooser(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.take_photo), R.drawable.ic_action_camera) {
                    @Override
                    public void run() {
                        MediaUtils.showPhotoCamera(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.record_video), R.drawable.ic_action_play) {
                    @Override
                    public void run() {
                        MediaUtils.showVideoRecorder(ChatActivity.this);
                    }
                },
                new ActionItem(getString(R.string.share_location), R.drawable.ic_action_share) {
                    @Override
                    public void run() {
                        // Response will be returned into ChatActivity.onNewIntent()
                        MediaUtils.requestMyLocation(ChatActivity.this);
                    }
                }
        };

        composeView.setActionItems(items);
    }

    // Start voice or video call
    private void startCall(boolean isVideo) {
        bit6.getCallClient().startCall(other, isVideo);
    }

}
