package com.bit6.samples.authfb;

import android.app.Application;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;


public class App extends Application {

    public final static int BIT6_ENV = Bit6.PRODUCTION;
    public final static String BIT6_API_KEY = "YOUR_API_KEY";

    @Override
    public void onCreate() {
        super.onCreate();
        Bit6 bit6 = Bit6.getInstance();
        bit6.init(getApplicationContext(), BIT6_API_KEY, BIT6_ENV);
        registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));
    }
}
