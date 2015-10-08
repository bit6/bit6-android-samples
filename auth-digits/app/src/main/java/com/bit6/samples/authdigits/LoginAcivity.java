package com.bit6.samples.authdigits;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

public class LoginAcivity extends AppCompatActivity {

    private Bit6 bit6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bit6 = Bit6.getInstance();

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(
                new AuthCallback() {
                    @Override
                    public void success(DigitsSession session, String phoneNumber) {
                        TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
                        TwitterAuthToken authToken = (TwitterAuthToken) session.getAuthToken();
                        DigitsOAuthSigning oauthSigning = new DigitsOAuthSigning(authConfig, authToken);
                        Map<String, String> authHeaders = oauthSigning.getOAuthEchoHeadersForVerifyCredentials();
                        bit6.getSessionClient().oauth("digits", authHeaders, authResultHandler);
                    }

                    @Override
                    public void failure(DigitsException ex) {
                    }
                }
        );
        digitsButton.setAuthTheme(R.style.AppTheme);
    }


    // Handle the Bit6 authentication result
    private ResultHandler authResultHandler = new ResultHandler() {

        @Override
        public void onResult(boolean success, String msg) {
        if (success) {
            // Is the user authenticated?
            if (bit6.getSessionClient().isAuthenticated()) {
                // Go the Main activity
                Intent intent = new Intent(LoginAcivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        } else {
            Log.e("LoginActivity", "Auth failed: " + msg);
        }
        if (msg != null) {
            Toast.makeText(LoginAcivity.this, msg, Toast.LENGTH_LONG).show();
        }
        }
    };

}
