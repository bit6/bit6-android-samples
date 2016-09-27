package com.bit6.samples.demo;

import android.content.Context;
import android.database.Cursor;

import com.bit6.sdk.db.Contract;
import com.bit6.ui.Contact;
import com.bit6.ui.ContactSource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Super simple ContactSource implementation. It converts a username identity into a display name.
 * It also uses the existing conversations from Bit6 to preload the list. Values are cached.
 */
public class MyContactSource implements ContactSource {

    private HashMap<String, Contact> contacts = new HashMap<>();

    // App-specific way to pre-populate the contacts list
    public void load(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Contract.Conversations.CONTENT_URI, new String[]{
                        Contract.Conversations.ID
                }, null, null, null);

        int identityIndex = cursor.getColumnIndex(Contract.Conversations.ID);

        while (cursor.moveToNext()) {
            String identity = cursor.getString(identityIndex);
            // TODO: What about groups?
            get(identity);
        }
        cursor.close();
    }

    // App-specific way of getting all the contacts to be displayed in the app UI
    public ArrayList<Contact> getContactsAsArrayList() {
        return new ArrayList<>(contacts.values());
    }

    @Override
    public Contact get(String identity) {
        // Get Contact from cache
        Contact c = contacts.get(identity);
        // Are we missing the cached contact?
        if (c == null) {
            // Create a new contact from identity
            String name = identity;
            int pos = identity.indexOf(':');
            if (pos > 0) {
                name = identity.substring(pos + 1);
                // Uppercase the first letter for a pretty display name
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            // Save it in cache
            contacts.put(identity, new MyContact(identity, name));
        }
        return c;
    }


    public class MyContact implements Contact {
        private String id, name;

        MyContact(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public String getAvatarUri() {
            return null;
        }
    }
}
