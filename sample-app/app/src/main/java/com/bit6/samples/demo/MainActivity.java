package com.bit6.samples.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.SessionClient;

public class MainActivity extends Activity implements OnClickListener {

    static final String TAG = "Main";

    private Bit6 bit6;
    private EditText mUsername;
    private EditText mPassword;
    private Button mLogin;
    private Button mSignup;

    // Handle the authentication result (from login or signup calls)
    private ResultHandler mAuthResultHandler = new ResultHandler() {

        @Override
        public void onResult(boolean success, String msg) {
            mLogin.setEnabled(true);
            mSignup.setEnabled(true);
            if (success) {
                if (doneIfAuthenticated()) {
                    // Do not show anything in the toast
                    msg = null;
                }
            } else {
                Log.e(TAG, "Auth failed: " + msg);
            }
            if (msg != null) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bit6 = Bit6.getInstance();

        // User credentials
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        // Login
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);

        // Signup
        mSignup = (Button) findViewById(R.id.signup);
        mSignup.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If user auth'ed - go to Chats
        doneIfAuthenticated();
    }

    // If user is authenticated - go to ChatsActivity
    private boolean doneIfAuthenticated() {
        // Is the user authenticated?
        SessionClient sessionClient = bit6.getSessionClient();
        boolean flag = sessionClient.isAuthenticated();
        if (flag) {
            // Go the Chats activity
            Address address = sessionClient.getOwnIdentity();
            String displayName = address.getValue();
            if (displayName != null) {
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            }
            bit6.getSessionClient().setDisplayName(displayName);
            Intent intent = new Intent(MainActivity.this, ChatsActivity.class);
            startActivity(intent);
            finish();
        }
        return flag;
    }

    @Override
    public void onClick(View v) {
        // Signup or Login clicked

        String username = mUsername.getText().toString().trim();
        String pass = mPassword.getText().toString().trim();

        // Invalid username or password
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getString(R.string.error_incorrect_credentials), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Disable login/signup button
        mLogin.setEnabled(false);
        mSignup.setEnabled(false);

        // User identity - we assume username kind
        Address identity = Address.fromParts(Address.KIND_USERNAME, username);

        // Signup
        if (v == mSignup) {
            bit6.getSessionClient().signup(identity, pass, mAuthResultHandler);
        }
        // Login
        else {
            bit6.getSessionClient().login(identity, pass, mAuthResultHandler);
        }
    }
}
