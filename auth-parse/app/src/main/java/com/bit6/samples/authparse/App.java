package com.bit6.samples.authparse;

import android.app.Application;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;
import com.parse.Parse;


public class App extends Application {

    public final static int BIT6_ENV = Bit6.PRODUCTION;
    public final static String BIT6_API_KEY = "YOUR_API_KEY",
            PARSE_APPLICATION_ID = "YOUR_APP_ID",
            PARSE_CLIENT_KEY = "YOUR_CLIENT_KEY";


    @Override
    public void onCreate() {
        super.onCreate();
        Bit6 bit6 = Bit6.getInstance();
        bit6.init(getApplicationContext(), BIT6_API_KEY, BIT6_ENV);
        registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
    }
}
