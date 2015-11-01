package com.timappweb.timapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.RestFeedback;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.concurrent.TimeUnit;

import retrofit.client.Response;


/**
 * @warnings DO NOT USE THIS CLASS. TO BE REMOVED
 * TODO remove this class
 */
public class CurrentSpotActivity extends AppCompatActivity {

    private static final String TAG = "CurrentSpotActivity";

    ProgressDialog dialog;
    Spot spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_spot);

        this.dialog = new ProgressDialog(this);

        // Check if there is data
        if (!Spot.isBroadcasting()){
            Log.i(TAG, "This spot is not in pref: ");
            return ; // TODO
        }

        spot = Spot.loadFromPref();
        // Check if it's expired
        if (spot.isExpired()){
            Log.i(TAG, "This spot is expired: " + spot.toString());
        }
        this._set();


        // Load the view
        Button b = (Button) findViewById(R.id.button_stop_broadcast);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.show();
                stopBroadcast();
            }
        });
    }

    protected void _set(){
        TextView textCreated = (TextView) findViewById(R.id.text_view_created);
        textCreated.setText(spot.getCreatedDate());

        TextView textExpired = (TextView) findViewById(R.id.text_view_expired);
        textCreated.setText(spot.getExpiredDate());

        TextView textTags = (TextView) findViewById(R.id.text_view_tags);
        textTags.setText(spot.tag_string);

        //this.initCountDownTimer();
    }

    private static final String FORMAT = "%02d:%02d:%02d";
/*
    protected void initCountDownTimer(){
        final TextView textRemaining = (TextView) findViewById(R.id.text_view_remaining);
        new CountDownTimer(spot.getRemainingTime() * 1000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                textRemaining.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                textRemaining.setText("done!");
            }
        }.start();
    }
*/
    private void stopBroadcast() {
        Spot.removeFromPref();
        dialog.hide();
        RestClient.instance().getService().stopBroadcast(new RestCallback<RestFeedback>() {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                Log.i(TAG, "Stop broadcasting on server side...");
                Intent intent = new Intent(getApplicationContext(), BroadcastActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_spot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_map){
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
