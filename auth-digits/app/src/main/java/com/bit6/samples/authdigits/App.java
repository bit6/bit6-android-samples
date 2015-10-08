
package com.bit6.samples.authdigits;

import android.app.Application;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;
import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    final static String
            DEV_API_KEY = "YOUR_API_KEY",
            DIGITS_CONSUMER_KEY = "YOUR_CONSUMER_KEY",
            DIGITS_CONSUMER_SECRET = "YOUR_CONSUMER_SECRET";

    public void onCreate() {
        super.onCreate();

        final TwitterAuthConfig authConfig = new TwitterAuthConfig(DIGITS_CONSUMER_KEY, DIGITS_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        Bit6 bit6 = Bit6.getInstance();
        // Initialize Bit6
        bit6.init(getApplicationContext(), DEV_API_KEY, Bit6.PRODUCTION);

        // Manage Bit6 lifecycle automatically
        registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));
    }

}
