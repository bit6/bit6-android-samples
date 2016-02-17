package com.bit6.samples.authparse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.SessionClient;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Bit6 bit6;
    private SessionClient sessionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bit6 = Bit6.getInstance();
        sessionClient = bit6.getSessionClient();
        if(sessionClient == null || !sessionClient.isAuthenticated()){
            goLoginScreen();
        }

        Button video = (Button)findViewById(R.id.video_call);
        video.setOnClickListener(this);
        Button voice = (Button)findViewById(R.id.voice_call);
        voice.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            sessionClient.logout();
            ParseUser.logOut();
            goLoginScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goLoginScreen(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_call:
                startCall(true);
                break;
            case R.id.voice_call:
                startCall(false);
                break;
            default:
                break;
        }
    }

    private void startCall(boolean isVideo){
        String other = ((EditText)findViewById(R.id.phone_number)).getText().toString();
        if (TextUtils.isEmpty(other)) {
            return;
        }

        Address to = Address.fromParts(Address.KIND_USERNAME, other);
        RtcDialog d = bit6.getCallClient().startCall(to, isVideo);
    }
}
