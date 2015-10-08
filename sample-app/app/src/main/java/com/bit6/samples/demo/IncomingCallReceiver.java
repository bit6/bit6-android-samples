
package com.bit6.samples.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;
import com.bit6.sdk.SessionClient;

public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SessionClient sessionClient = Bit6.getInstance().getSessionClient();
        if(sessionClient == null || !sessionClient.isAuthenticated()){
            return;
        }
        Intent i = null;
        CallClient callClient = Bit6.getInstance().getCallClient(); 
        if (callClient != null && callClient.getRtcDialogs().size() > 0) {
            i = new Intent(context, CallActivity.class);
        } else {
            i = new Intent(context, IncomingCallActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Send Call information to the IncomingCallActivity
        i.putExtras(intent);
        context.startActivity(i);
    }
}
