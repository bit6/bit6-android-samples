
package com.bit6.samples.demo;

import android.app.Application;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;
import com.bit6.ui.IncomingMessageReceiver;

public class App extends Application {

    final static String
            PROD_API_KEY = "YOUR_API_KEY";

    private MyContactSource contactSource;

    public void onCreate() {
        super.onCreate();

        Bit6 bit6 = Bit6.getInstance();

        // Initialize Bit6
        bit6.init(getApplicationContext(), PROD_API_KEY);

        // Manage Bit6 lifecycle automatically
        registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));

        // Create the app-specific ContactSource that will be used
        // to render contact information in Bit6 UI components
        contactSource = new MyContactSource();
        contactSource.load(this);
        IncomingMessageReceiver.setContactSource(contactSource);
    }

    public MyContactSource getContactSource() {
        return contactSource;
    }
}
