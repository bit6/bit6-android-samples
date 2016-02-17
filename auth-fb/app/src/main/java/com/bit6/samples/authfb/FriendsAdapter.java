package com.bit6.samples.authfb;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;

import org.json.JSONArray;
import org.json.JSONObject;

public class FriendsAdapter extends
        RecyclerView.Adapter<FriendsAdapter.ViewHolder>{

    private JSONArray friends;

    FriendsAdapter(JSONArray users) {
        this.friends = users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton callButton;
        ImageButton voiceCallButton;
        View item;

        public ViewHolder(View itiemView) {
            super(itiemView);
            name = (TextView) itiemView.findViewById(R.id.name);
            callButton = (ImageButton) itiemView.findViewById(R.id.call);
            voiceCallButton = (ImageButton) itiemView.findViewById(R.id.voice_call);
            item = itiemView;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        JSONObject user = friends.optJSONObject(i);
        String userId = user.optString("id");
        String name = user.optString("name");
        viewHolder.name.setText(name);

        viewHolder.voiceCallButton.setOnClickListener(new OnItemClickListener(userId, false));
        viewHolder.item.setOnClickListener(new OnItemClickListener(userId, true));

    }

    @Override
    public int getItemCount() {
        if (friends != null) {
            return friends.length();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (friends != null && friends.length() > 0) {
            JSONObject user = friends.optJSONObject(position);
            if (user != null) {
                return Long.parseLong(user.optString("id"));
            }
        }
        return -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.contact_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    class OnItemClickListener implements View.OnClickListener {

        private String userId;
        private boolean isVideo;

        public OnItemClickListener(String userId, boolean isVideo) {
            this.userId = userId;
            this.isVideo = isVideo;
        }

        @Override
        public void onClick(View v) {
            Bit6 bit6 = Bit6.getInstance();
            Address to = Address.fromParts("fb", userId);
            bit6.getCallClient().startCall(to, isVideo);
        }
    }

    public void setData(JSONArray users) {
        this.friends = users;
    }
}
