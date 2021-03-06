package com.delricco.vince.revolardemo.twitter;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.delricco.vince.revolardemo.R;
import com.delricco.vince.revolardemo.twitter.requests.AuthTokenRequest;
import com.delricco.vince.revolardemo.twitter.requests.TwitterTimelineRequest;
import com.delricco.vince.revolardemo.twitter.requests.TwitterUserRequest;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.thefinestartist.finestwebview.FinestWebView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * All Twitter code adapted from: https://github.com/Rockncoder/TwitterTutorial
 */
public class TwitterActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    final static String TAG = TwitterActivity.class.getSimpleName();

    private Twitter twits;
    private TwitterAdapter adapter;
    private RequestQueue requestQueue;
    private Authenticated authenticated;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(this);
        twits = new Twitter();
        adapter = new TwitterAdapter();
        setContentView(R.layout.activity_twitter);
        ListView twitterTimeline = (ListView) findViewById(R.id.twitter_timeline);
        twitterTimeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tweetUrl = getString(R.string.revolar_status_url) + twits.get(i).getId();
                new FinestWebView.Builder(getApplicationContext()).show(tweetUrl);
            }
        });
        twitterTimeline.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        authenticated = new Authenticated("", "");
        downloadTweets();
    }

    @Override
    public void onRefresh() {
        twits.clear();
        adapter.notifyDataSetChanged();
        downloadTweets();
    }

    /* Download twitter timeline after first checking to see if there is a network connection */
    public void downloadTweets() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            /* Begins the process of grabbing Twitter timeline. Next steps are handled
               by Volley response listeners */
            if (isAuthenticated(authenticated)) {
                /* If we're already authenticated, just request the twitter timeline */
                Request twitterTimelineRequest = new TwitterTimelineRequest(
                        getString(R.string.twitter_timeline_url) + getString(R.string.revolar),
                        new TwitterTimelineResponseListener(),
                        new GenericErrorListener(),
                        authenticated);
                requestQueue.add(twitterTimelineRequest);
            } else {
                getAuthToken();
            }
        } else {
            Log.v(TAG, "No network connection available.");
        }
    }

    private Twitter jsonToTwitter(String result) {
        Twitter twits = null;
        if (result != null && result.length() > 0) {
            try {
                Log.i(TAG, result);
                twits = new Gson().fromJson(result, Twitter.class);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        return twits;
    }

    private void jsonToAuthenticated(String rawAuthorization) {
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                authenticated = new Gson().fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isAuthenticated(Authenticated auth) {
        return (auth.getTokenType().equals("bearer"));
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

    private class TwitterTimelineResponseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            twits.clear();
            twits.addAll(jsonToTwitter(response));
            adapter.notifyDataSetChanged();
            if (swipeLayout.isRefreshing()) {
                swipeLayout.setRefreshing(false);
            }
        }
    }

    private class AuthResponseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            jsonToAuthenticated(response);
            /* Applications should verify that the value associated with the
               token_type key of the returned object is bearer */
            if (isAuthenticated(authenticated)) {
                Request twitterTimelineRequest = new TwitterTimelineRequest(
                        getString(R.string.twitter_timeline_url) + getString(R.string.revolar),
                        new TwitterTimelineResponseListener(),
                        new GenericErrorListener(),
                        authenticated);
                requestQueue.add(twitterTimelineRequest);
            }
        }
    }

    private class GenericErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    }

    class TwitterAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        TwitterAdapter() {
            inflater = (LayoutInflater) TwitterActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return twits.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null)
                view = inflater.inflate(R.layout.twitter_row, null);

            /* Remove the "_normal" from the URL to get a full size picture. We will shrink it */
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
                                String profPicUrl = user.getProfileImageUrl().replace("_normal", "");
                                Picasso.with(TwitterActivity.this.getApplicationContext())
                                        .load(profPicUrl).into(profPic);
                            }
                        },
                        new GenericErrorListener(),
                        authenticated);
                requestQueue.add(twitterUserRequest);
            } else {
                retweetLayout.setVisibility(View.GONE);
                twitterNameTv.setText(getString(R.string.preceding_at_sign, twitterName));
                tweetTv.setText(tweet);
                Picasso.with(TwitterActivity.this.getApplicationContext())
                        .load(twitterProfPicURL).into(profPic);
            }

            return view;
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
