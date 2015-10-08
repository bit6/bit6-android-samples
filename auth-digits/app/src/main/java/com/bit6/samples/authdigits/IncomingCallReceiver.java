
package com.bit6.samples.authdigits;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bit6.sdk.Bit6;

public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bit6 bit6 = Bit6.getInstance();
        if(bit6.getSessionClient() == null || !bit6.getSessionClient().isAuthenticated()){
            return;
        }
        Intent i = new Intent(context, IncomingCallActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Send Call information to the IncomingCallActivity
        i.putExtras(intent);
        context.startActivity(i);
    }
}
