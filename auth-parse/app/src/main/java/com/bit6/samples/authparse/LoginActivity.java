package com.bit6.samples.authparse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.SessionClient;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username;
    private EditText password;
    private Button login;
    private Button signup;
    private Bit6 bit6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bit6 = Bit6.getInstance();

        // User credentials
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        // Login
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);

        // Signup
        signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String usrName = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        // Invalid username or password
        if (TextUtils.isEmpty(usrName) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getString(R.string.incorrect_credentials), Toast.LENGTH_LONG).show();
            return;
        }

        // Disable login/signup button
        login.setEnabled(false);
        signup.setEnabled(false);
        if (v == signup) {
            ParseUser user = new ParseUser();
            user.setUsername(usrName);
            user.setPassword(pass);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        doExternalLogin();
                    } else {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        login.setEnabled(true);
                        signup.setEnabled(true);
                    }
                }
            });
        } else if (v == login) {
            ParseUser.logInInBackground(usrName, pass, new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        doExternalLogin();
                    } else {
                        if (e != null) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            login.setEnabled(true);
                            signup.setEnabled(true);
                        }
                    }
                }
            });
        }
    }

    private void doExternalLogin() {
        final Map<String, String> map = new HashMap<String, String>();
        ParseCloud.callFunctionInBackground("bit6_auth", map, new FunctionCallback<String>() {
            @Override
            public void done(String s, ParseException e) {
                if (e == null) {
                    SessionClient sessionClient = bit6.getSessionClient();
                    sessionClient.external(s, new ResultHandler() {
                        @Override
                        public void onResult(boolean success, String msg) {
                            login.setEnabled(true);
                            signup.setEnabled(true);
                            if(success){
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        }
                    });
                } else {
                    if (e != null) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        login.setEnabled(true);
                        signup.setEnabled(true);
                    }
                }
            }
        });
    }
}
