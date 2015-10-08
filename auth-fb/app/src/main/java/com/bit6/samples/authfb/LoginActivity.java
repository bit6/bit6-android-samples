package com.bit6.samples.authfb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private Bit6 bit6;
    private CallbackManager callbackManager;
    private ProgressBar progress;

    private static final List<String> PERMISSIONS = new ArrayList<String>() {

        private static final long serialVersionUID = 1L;

        {
            add("user_friends");
            add("public_profile");
            add("email");
        }
    };

    private FacebookCallback<LoginResult> loginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            onLoginSuccess(loginResult);
        }

        @Override
        public void onCancel() {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onError(FacebookException exception) {
            progress.setVisibility(View.GONE);
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        bit6 = Bit6.getInstance();

        progress = (ProgressBar)findViewById(R.id.progress);

        Button login = (Button) findViewById(R.id.fb_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                doFbLogin();
            }
        });

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, loginCallback);
    }

    protected void onLoginSuccess(LoginResult loginResult) {
        sendAccessToken(loginResult.getAccessToken());
    }

    protected void sendAccessToken(final AccessToken token) {
        if (token == null) {
            return;
        }

        bit6.getSessionClient().oauth("facebook", token.getToken(), new ResultHandler() {

            @Override
            public void onResult(boolean success, String msg) {
                if (success) {
                    progress.setVisibility(View.GONE);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void doFbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
