package com.bit6.samples.demo;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.db.Contract.Members;
import com.bit6.ui.Contact;
import com.bit6.ui.ContactSource;

public class MemberAdapter extends CursorAdapter {

    ResultHandler handler = new ResultHandler() {
        @Override
        public void onResult(boolean success, String msg) {
            if (!success) {
                Log.e("Leave group", msg);
            }
        }
    };
    private String groupId;
    private ContactSource cs;

    public MemberAdapter(Context context, Cursor c, String groupId, ContactSource source, boolean autoRequery) {
        super(context, c, autoRequery);
        this.groupId = groupId;
        this.cs = source;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.group_member_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView id = (TextView) view.findViewById(R.id.member_id);
        TextView role = (TextView) view.findViewById(R.id._role);
        Button action = (Button) view.findViewById(R.id.action);

        String memberId = cursor.getString(cursor.getColumnIndex(Members.ID));
        String memberRole = cursor.getString(cursor.getColumnIndex(Members.ROLE));

        // TODO: need to find a better way. User's identity in a group can be anything:
        // usr:john, fb:123 etc. It is shown as a part of membership info during
        // GET /me?embed=groups
        // Maybe the easiest for now show 'kick' near everybody?
        //if (memberId.startsWith(Address.KIND_USERID)) {
        //    Address add = Address.parse(memberId);
        //    if (add != null && add.getValue().equalsIgnoreCase(sessionClient.getUserId())) {
        //        memberId = sessionClient.getOwnIdentity().toString();
        //    }
        //}

        action.setOnClickListener(new MyOnClickListener(memberId));

        String memberName = memberId;
        Contact c = cs != null ? cs.get(memberId) : null;
        if (c != null) {
            memberName = c.getDisplayName();
        }

        id.setText(memberName);
        role.setText(memberRole);
    }

    class MyOnClickListener implements View.OnClickListener {

        private String ident;

        public MyOnClickListener(String ident) {
            this.ident = ident;
        }

        @Override
        public void onClick(View v) {
            Bit6.getInstance().kickGroupMember(groupId, ident, handler);
        }
    }

}
