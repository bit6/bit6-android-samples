package com.bit6.samples.authfb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.SessionClient;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    private Bit6 bit6;
    private FriendsAdapter adapter;

    protected static JSONArray friends = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.setApplicationId(getString(R.string.fb_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        bit6 = Bit6.getInstance();
        SessionClient sessionClient = bit6.getSessionClient();
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (sessionClient != null && sessionClient.isAuthenticated() && token != null) {
            requestFacebookFriends(token);
        } else {
            openLoginScreen();
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new FriendsAdapter(friends);
        recyclerView.setAdapter(adapter);
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
            LoginManager.getInstance().logOut();
            if (AccessToken.getCurrentAccessToken() != null) {
                AccessToken.setCurrentAccessToken(null);
            }
            bit6.getSessionClient().logout();
            openLoginScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void requestFacebookFriends(AccessToken token) {
        Bundle params = new Bundle();
        params.putString("fields", "id, name");

        GraphRequest req = GraphRequest.newMyFriendsRequest(
                token,
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(
                            JSONArray jsonArray,
                            GraphResponse response) {
                        friends = jsonArray;
                        onFriendsListReceived();
                    }
                });

        req.setParameters(params);
        req.executeAsync();
    }

    protected void onFriendsListReceived() {
        if (friends == null) {
            return;
        }

        if (adapter != null) {
            adapter.setData(friends);
            adapter.notifyDataSetChanged();
        }
    }

    private void openLoginScreen() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

}
