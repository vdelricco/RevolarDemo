package com.delricco.vince.revolardemo.twitter;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.delricco.vince.revolardemo.R;
import com.delricco.vince.revolardemo.util.BitmapLruCache;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * All Twitter code adapted from: https://github.com/Rockncoder/TwitterTutorial
 */
public class TwitterActivity extends ListActivity {

    final static String TAG = TwitterActivity.class.getSimpleName();

    private Twitter twits;
    private TwitterAdapter adapter;
    private RequestQueue requestQueue;
    private Authenticated authenticated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(this);
        twits = new Twitter();
        adapter = new TwitterAdapter();
        setListAdapter(adapter);
        authenticated = null;
        downloadTweets();
    }

    /* Download twitter timeline after first checking to see if there is a network connection */
    public void downloadTweets() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            /* Begins the process of grabbing Twitter timeline. Next steps are handled
               by Volley response listeners */
            getAuthToken();
        } else {
            Log.v(TAG, "No network connection available.");
        }
    }

    private Twitter jsonToTwitter(String result) {
        Twitter twits = null;
        if (result != null && result.length() > 0) {
            try {
                twits = new Gson().fromJson(result, Twitter.class);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        return twits;
    }

    private Authenticated jsonToAuthenticated(String rawAuthorization) {
        Authenticated auth = null;
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                auth = new Gson().fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        return auth;
    }

    private void getAuthToken() {
        /* Step 1: Encode consumer key and secret */
        try {
            /* URL encode the consumer key and secret */
            String urlApiKey = URLEncoder.encode(getString(R.string.twitter_consumer_key), "UTF-8");
            String urlApiSecret = URLEncoder.encode(getString(R.string.twitter_consumer_secret), "UTF-8");

            /* Concatenate the encoded consumer key, a colon character, and the
               encoded consumer secret */
            final String combined = urlApiKey + ":" + urlApiSecret;

            /* Base64 encode the string */
            final String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            /* Request token with the base64 encoded key & secret */
            Request authTokenRequest = new AuthTokenRequest(
                    getString(R.string.twitter_auth_token_url),
                    new AuthResponseListener(),
                    new GenericErrorListener(),
                    base64Encoded);
            requestQueue.add(authTokenRequest);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }
    private class TwitterUserRequest extends StringRequest {

        private TwitterUserRequest(String url,
                                   Response.Listener<String> listener,
                                   Response.ErrorListener errorListener)
        {
            super(Request.Method.GET, url, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + authenticated.access_token);
            headers.put("Content-Type", "application/json");
            return headers;
        }
    }

    private class AuthTokenRequest extends StringRequest {
        private String encodedKeyAndSecret;

        private AuthTokenRequest(String url,
                                 Response.Listener<String> listener,
                                 Response.ErrorListener errorListener,
                                 String encodedKeyAndSecret)
        {
            super(Request.Method.POST, url, listener, errorListener);
            this.encodedKeyAndSecret = encodedKeyAndSecret;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Length", String.valueOf(getBody().length));
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            headers.put("Authorization", "Basic " + encodedKeyAndSecret);
            return headers;
        }

        @Override
        public byte[] getBody() {
            return ("grant_type=client_credentials").getBytes();
        }
    }

    private class TwitterTimelineRequest extends StringRequest {
        private Authenticated auth;

        private TwitterTimelineRequest(String url,
                                       Response.Listener<String> listener,
                                       Response.ErrorListener errorListener,
                                       Authenticated auth) {
            super(Method.GET, url, listener, errorListener);
            this.auth = auth;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + auth.access_token);
            headers.put("Content-Type", "application/json");
            return headers;
        }
    }

    private class TwitterTimelineResponseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            twits.clear();
            twits.addAll(jsonToTwitter(response));
            adapter.notifyDataSetChanged();
        }
    }

    private class AuthResponseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            final Authenticated auth = jsonToAuthenticated(response);
            /* Applications should verify that the value associated with the
               token_type key of the returned object is bearer */
            if (auth != null && auth.token_type.equals("bearer")) {
                authenticated = auth;
                Request twitterTimelineRequest = new TwitterTimelineRequest(
                        getString(R.string.twitter_timeline_url) + getString(R.string.revolar),
                        new TwitterTimelineResponseListener(),
                        new GenericErrorListener(),
                        auth
                );
                requestQueue.add(twitterTimelineRequest);
            }
        }
    }

    private class GenericErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) { Log.e(TAG, error.toString()); }
    }

    class TwitterAdapter extends BaseAdapter {
        BitmapLruCache bitmapLruCache;

        private LayoutInflater inflater;

        TwitterAdapter() {
            inflater = (LayoutInflater) TwitterActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            bitmapLruCache = new BitmapLruCache();
        }

        @Override
        public int getCount() {
            return twits.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ImageLoader imageLoader = new ImageLoader(requestQueue, bitmapLruCache);

            if (convertView == null)
                view = inflater.inflate(R.layout.twitter_row, null);

            String twitterProfPicURL = twits.get(position).getUser().getProfileImageUrl().replace("_normal", "");
            String twitterName = twits.get(position).getUser().getName();
            String tweet = twits.get(position).getText();

            LinearLayout retweetLayout = (LinearLayout) view.findViewById(R.id.retweeted_layout);
            TextView twitterNameTv = (TextView) view.findViewById(R.id.twitter_name);
            TextView tweetTv = (TextView) view.findViewById(R.id.tweet);
            final ImageView profPic = (ImageView) view.findViewById(R.id.twitter_prof_pic);

            /* Check if we're dealing with a retweet */
            if (tweet.startsWith("RT")) {
                retweetLayout.setVisibility(View.VISIBLE);
                String retweetedAccountName = tweet.substring(tweet.indexOf("@") + 1, tweet.indexOf(":"));
                twitterNameTv.setText(getString(R.string.preceding_at_sign, retweetedAccountName));
                tweet = tweet.replace("RT " + getString(R.string.preceding_at_sign, retweetedAccountName) + ": ", "");
                tweetTv.setText(tweet);
                Request twitterUserRequest = new TwitterUserRequest(
                        getString(R.string.twitter_user_url) + retweetedAccountName,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                TwitterUser user = new Gson().fromJson(response, TwitterUser.class);
                                ImageLoader il = new ImageLoader(requestQueue, bitmapLruCache);
                                il.get(user.getProfileImageUrl().replace("_normal", ""), new ProfPicImageListener(profPic));
                            }
                        },
                        new GenericErrorListener());
                requestQueue.add(twitterUserRequest);
            } else {
                retweetLayout.setVisibility(View.GONE);
                twitterNameTv.setText(getString(R.string.preceding_at_sign, twitterName));
                tweetTv.setText(tweet);
                imageLoader.get(twitterProfPicURL, new ProfPicImageListener(profPic));
            }

            return view;
        }

        private class ProfPicImageListener implements ImageLoader.ImageListener {
            ImageView profPicIv;

            ProfPicImageListener(ImageView profPicIv) {
                this.profPicIv = profPicIv;
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    Bitmap profPicBitmap = response.getBitmap();
                    profPicBitmap = Bitmap.createScaledBitmap(profPicBitmap, 256, 256, false);
                    profPicIv.setImageBitmap(profPicBitmap);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
