package com.bit6.samples.authdigits;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.SessionClient;
import com.digits.sdk.android.Digits;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Bit6 bit6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bit6 = Bit6.getInstance();
        SessionClient sessionClient = bit6.getSessionClient();
        if(sessionClient == null || !sessionClient.isAuthenticated()){
            openLoginScreen();
        }

        Button video = (Button)findViewById(R.id.video_call);
        video.setOnClickListener(this);
        Button voice = (Button)findViewById(R.id.voice_call);
        voice.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            bit6.getSessionClient().logout();
            Digits.getSessionManager().clearActiveSession();
            openLoginScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLoginScreen(){
        Intent i = new Intent(this, LoginAcivity.class);
        startActivity(i);
        finish();
    }

    private void startCall(boolean isVideo){
        String phoneNumber = ((EditText)findViewById(R.id.phone_number)).getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 7) {
            Toast.makeText(this, R.string.error_number, Toast.LENGTH_LONG).show();
            return;
        }
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+"+phoneNumber;
        }
        Address to = Address.fromParts(Address.KIND_PHONE, phoneNumber);
        if(to != null){
            bit6.getCallClient().startCall(to, isVideo, null);
        }else{
            Toast.makeText(this, R.string.error_number, Toast.LENGTH_LONG).show();
        }
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
}
