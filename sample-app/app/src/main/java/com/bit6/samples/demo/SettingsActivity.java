package com.bit6.samples.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.SessionClient;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Bit6 bit6 = Bit6.getInstance();

        Spinner mediaSpinner = (Spinner) findViewById(R.id.media_mode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.call_mode_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mediaSpinner.setAdapter(adapter);
        settings = new Settings(this);
        int mode = settings.getMediaMode();
        mediaSpinner.setSelection(Settings.MODE_P2P == mode ? 0 : 1);
        mediaSpinner.setOnItemSelectedListener(this);

        TextView env = (TextView) findViewById(R.id.env);
        String envStr;
        if (settings.getEnvironment() == Settings.DEVELOPMENT) {
            envStr = getString(R.string.dev);
        } else {
            envStr = getString(R.string.prod);
        }
        env.setText(envStr);

        TextView apiKey = (TextView) findViewById(R.id.api_key);
        apiKey.setText(App.PROD_API_KEY);

        TextView rt = (TextView) findViewById(R.id.rt_connected);
        rt.setText(Bit6.getInstance().isWebSocketConnected() ? getString(R.string.connected) : getString(R.string.disconnected));

        TextView myUser = (TextView) findViewById(R.id.my_user);
        TextView displayName = (TextView) findViewById(R.id.display_name);
        SessionClient sessionClient = Bit6.getInstance().getSessionClient();
        if(sessionClient != null){
            Address address = sessionClient.getOwnIdentity();
            if(address != null){
                myUser.setText(address.toString());
            }

            displayName.setText(sessionClient.getDisplayName());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Environment selection
        boolean isP2p = position == 0;
        int mode = isP2p ? Settings.MODE_P2P : Settings.MODE_MIX;
        settings.setMediaMode(mode);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}
