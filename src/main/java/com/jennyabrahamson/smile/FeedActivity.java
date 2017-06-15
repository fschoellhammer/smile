package com.jennyabrahamson.smile;

import android.content.Context;
import android.os.AsyncTask;
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import io.fabric.sdk.android.Fabric;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;
import uk.co.senab.bitmapcache.BitmapLruCache;

import java.net.URLEncoder;
import java.util.*;


public class FeedActivity extends ActionBarActivity {
    private ProgressBar progressBar;
    private AsyncListView listView;
    private Calendar lastDownloadedStoryDate;
    private FeedObject adapter;
    private MenuItem reloadItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.feed);

        BitmapLruCache cache = App.getInstance(this).getBitmapCache();
        FeedImageLoader loader = new FeedImageLoader(cache);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
        builder.setThreadPoolSize(4);

        listView = (AsyncListView) findViewById(R.id.list);
        listView.setItemManager(builder.build());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (view.getId() == listView.getId()) {
                    final int currentFirstVisibleItem = listView.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        getSupportActionBar().hide();
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        getSupportActionBar().show();
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        });

        new LoadFeedTask().execute();

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        progressBar.setPadding(0, 0, 40, 0);
    }

    private class LoadFeedTask extends AsyncTask<Void, Void, List<StoryItem>> {
        private static final String BASE_FEED_URL = "http://www.thirtypixel.com/smile/stories.aspx";
        private static final String DEFAULT_ENCODING = "utf-8";

        private String getQueryString(String baseUrl, Map<String, String> params) {
            StringBuilder sb = new StringBuilder(baseUrl);
            boolean first = true;

            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                    sb.append("?");
                } else {
                    sb.append("&");
                }

                try {
                    sb.append(URLEncoder.encode(entry.getKey(), DEFAULT_ENCODING))
                            .append("=")
                            .append(URLEncoder.encode(String.valueOf(entry.getValue()),
                                    DEFAULT_ENCODING));
                } catch (Exception e) {
                    continue;
                }
            }

            return sb.toString();
        }

        @Override
        protected List<StoryItem> doInBackground(Void... params) {
            List<StoryItem> stories = new ArrayList<StoryItem>();

            Map<String, String> urlParams = new HashMap();
            if (lastDownloadedStoryDate != null) {
                urlParams.put("start_before", "" + lastDownloadedStoryDate.getTimeInMillis());
            }
            String URL = getQueryString(BASE_FEED_URL, urlParams);
            JSONArray data = HttpHelper.loadJSON(URL);
            try {
                if (data == null) {
                    return stories;
                }
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    StoryItem si = StoryItem.initWithJson(obj);
                    if (si == null) {
                        continue;
                    }
                    if (si.isAnimated()) {
                        // TODO: no animation support yet
                        continue;
                    }
                    lastDownloadedStoryDate = si.getPostedDate();
                    stories.add(si);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Collections.sort(stories, new Comparator<StoryItem>() {
                @Override
                public int compare(StoryItem lhs, StoryItem rhs) {
                    return rhs.getPostedDate().compareTo(lhs.getPostedDate());
                }
            });

            return stories;
        }

        @Override
        protected void onPostExecute(List<StoryItem> stories) {
            if (adapter == null) {
                adapter = new FeedObject(FeedActivity.this, stories);
                listView.setAdapter(adapter);
            } else {
                adapter.addStories(stories);
                if (reloadItem != null) {
                    reloadItem.setActionView(null);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this, "671640982961917");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this, "671640982961917");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                item.setActionView(progressBar);
                reloadItem = item;
                new LoadFeedTask().execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
