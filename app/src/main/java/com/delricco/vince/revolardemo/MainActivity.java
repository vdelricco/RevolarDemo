package com.delricco.vince.revolardemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.delricco.vince.revolardemo.contacts.EditContactsActivity;
import com.delricco.vince.revolardemo.contacts.RevolarContact;
import com.delricco.vince.revolardemo.twitter.TwitterActivity;
import com.delricco.vince.revolardemo.util.AppPreferences;
import com.thefinestartist.finestwebview.FinestWebView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 2;
    private AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new AppPreferences(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ImageView imageView = (ImageView) findViewById(R.id.revolar_main_logo);
        /* Let the click listener handle sending SMS messages. Touch listener will
           handle imageview filter when the revolar image is touched */
        imageView.setOnClickListener(this);
        imageView.setOnTouchListener(new View.OnTouchListener() {

            /* onTouch code from here: http://stackoverflow.com/a/14483533 */
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //overlay is black with transparency of 0x77 (119)
                        imageView.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        imageView.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        imageView.getDrawable().clearColorFilter();
                        imageView.invalidate();
                        break;
                    }
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
           automatically handle clicks on the Home/Up button, so long
           as you specify a parent activity in AndroidManifest.xml */
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit_contacts:
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, EditContactsActivity.class));
                } else {
                    askForReadContactsPermission();
                }
                break;
            case R.id.action_view_twitter:
                startActivity(new Intent(this, TwitterActivity.class));
                break;
            case R.id.action_visit_website:
                new FinestWebView.Builder(this).show(getString(R.string.revolar_website));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        switch (requestCode) {
            /* If request is cancelled, the result arrays are empty. */
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, EditContactsActivity.class));
                }
                break;
            }

            case PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSelectedContactsSMS();
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.revolar_main_logo:
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    sendSelectedContactsSMS();
                } else {
                    askForSendSmsPermission();
                }
                break;
        }
    }

    private void askForReadContactsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    private void askForSendSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                PERMISSIONS_REQUEST_SEND_SMS);
    }

    public void sendSelectedContactsSMS() {
        Log.v(TAG, getString(R.string.sending_sms_messages));
        SmsManager smsManager = SmsManager.getDefault();
        List<RevolarContact> savedContacts = preferences.getContacts();

        if (!savedContacts.isEmpty()) {
            for (int i = 0; i < savedContacts.size(); i++) {
                try {
                    smsManager.sendTextMessage(savedContacts.get(i).getNumber(), null, getString(R.string.alert_sms_message), null, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_contacts), Toast.LENGTH_LONG).show();
        }
    }

    /* Debug method */
    public void printSavedContacts() {
        List<RevolarContact> contacts = preferences.getContacts();
        Log.v(TAG, "Contacts size is " + contacts.size());
        for (int i = 0; i < contacts.size(); i++) {
            Log.v(TAG, "Contact #" + i);
            Log.v(TAG, "Name = " + contacts.get(i).getName());
            Log.v(TAG, "Number = " + contacts.get(i).getNumber());
        }
    }
}
