package com.jennyabrahamson.smile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facebook.ads.*;
import com.facebook.appevents.AppEventsLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class FeedObject extends BaseAdapter {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a");
    private int blueColor = Color.parseColor("#66CCFF");
    private int grayColor = Color.parseColor("#808080");
    private List<StoryItem> stories;
    private AppEventsLogger logger;
    private Activity activity;
    private LayoutInflater layoutInflater;
    private List<NativeAdScrollView> adViews;
    private static final int ADS_PER_HSCROLL = 4;
    private static final int NUM_HSCROLL = 4;
    private static final int AD_INTERVAL = 5;

    private List<NativeAdsManager> adManagers;

    public FeedObject(final Activity context, List<StoryItem> stories) {

        activity = context;
        this.stories = stories;

        final DisplayMetrics metrics = activity.getResources().getDisplayMetrics();

        logger = AppEventsLogger.newLogger(activity, "671640982961917");

        layoutInflater =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        adViews = new ArrayList<NativeAdScrollView>();

        adManagers = new ArrayList<NativeAdsManager>();
        for (int i = 0; i < NUM_HSCROLL; i++) {
            final NativeAdsManager adManager = new NativeAdsManager(activity, "671640982961917_671644029628279", ADS_PER_HSCROLL);
            adManagers.add(adManager);
            adManager.setListener(new NativeAdsManager.Listener() {
                @Override
                public void onAdsLoaded() {
                    int numAds = adManager.getUniqueNativeAdCount();
                    Map dimensions = new HashMap();
                    dimensions.put("numAds", "" + numAds);
                    logEvent("adsLoaded", dimensions);
                    NativeAdScrollView scrollView = new NativeAdScrollView(activity, adManager,
                            NativeAdView.Type.HEIGHT_300, new NativeAdViewAttributes()
                            .setButtonTextColor(Color.WHITE)
                            .setButtonBorderColor(0xff8bc615)
                            .setButtonColor(0xff8bc615),
                            ADS_PER_HSCROLL);
                    scrollView.setInset(20);
                    scrollView.setPadding(
                            0, Math.round(5 * metrics.density), 0, Math.round(5 * metrics.density));
                    adViews.add(0, scrollView);

                    notifyUi();
                }

                @Override
                public void onAdError(AdError adError) {
                    Map<String, String> dimensions = new HashMap();
                    dimensions.put("error", adError.getErrorMessage());
                    logEvent("adError", dimensions);
                }
            });
            adManager.loadAds();
        }

        logEvent("adRequest", null);
    }

    private void notifyUi() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void addStories(List<StoryItem> newStories) {
        if (newStories != null && !newStories.isEmpty()) {
            this.stories.addAll(newStories);
            Collections.sort(stories, new Comparator<StoryItem>() {
                @Override
                public int compare(StoryItem lhs, StoryItem rhs) {
                    return rhs.getPostedDate().compareTo(lhs.getPostedDate());
                }
            });
            notifyUi();
        }
    }

    @Override
    public int getCount() {
        return adViews.size() + stories.size();
    }

    @Override
    public Object getItem(int position) {
        if (stories.isEmpty()) {
            return null;
        }

        if (adViews.isEmpty()) {
            return stories.get(position);
        }

        if (position == 0) {
            return stories.get(position);
        }

        if (position % AD_INTERVAL == 0) {
            return adViews.get(position % adViews.size());
        }

        return stories.get(position - (position / AD_INTERVAL));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object o = getItem(position);
        if (o instanceof StoryItem) {
            return renderStoryItem((StoryItem) o, convertView, parent, position);
        } else if (o instanceof NativeAdScrollView) {
            Map dimensions = new HashMap();
            dimensions.put("positionInFeed", "" + position);
            logEvent("scrollViewImp", dimensions);
            return (NativeAdScrollView) o;
        } else {
            return null;
        }
    }

    private View renderStoryItem(final StoryItem story, View convertView, ViewGroup parent, int position) {
        final ViewHolder holder;
        LinearLayout itemView;
        if (convertView != null && !(convertView instanceof NativeAdScrollView)) {
            itemView = (LinearLayout) convertView;
            holder = (ViewHolder) convertView.getTag();
        } else {
            itemView = (LinearLayout) layoutInflater.inflate(R.layout.feed_story, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) itemView.findViewById(R.id.title);
            holder.date = (TextView) itemView.findViewById(R.id.date);
            holder.image = (CustomImageView) itemView.findViewById(R.id.image);
            holder.votes = (TextView) itemView.findViewById(R.id.votes);
            holder.likeButton = (TextView) itemView.findViewById(R.id.like);
            itemView.setTag(holder);
        }

        holder.title.setText(story.getTitle());
        holder.date.setText(sdf.format(story.getPostedDate().getTime()));

        if (story.getImageUrl() != null) {
            new DownloadImageTask(position, holder)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, story.getImageUrl());
        }

        holder.votes.setText(story.getVotes() + " votes");

        if (story.getIsLiked()) {
            holder.likeButton.setTextColor(blueColor);
        } else {
            holder.likeButton.setTextColor(grayColor);
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean liked = story.getIsLiked();
                story.setLiked(!liked);
                if (story.getIsLiked()) {
                    holder.likeButton.setTextColor(blueColor);
                } else {
                    holder.likeButton.setTextColor(grayColor);
                }
                holder.votes.setText(story.getVotes() + " votes");
            }
        });

        return itemView;
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;

        public DownloadImageTask(int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            try {
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                entity.consumeContent();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mHolder.position == mPosition) {
                mHolder.image.setImageBitmap(bitmap);
            }
        }
    }

    class ViewHolder {
        public TextView title;
        public TextView date;
        public TextView votes;
        public TextView likeButton;

        public CustomImageView image;
        public int position;
    }

    private void logEvent(String event, Map<String, String> dimensions) {
        logger.logEvent(event);

        if (dimensions != null) {
            for (Map.Entry<String, String> entry : dimensions.entrySet()) {
                logger.logEvent(event + ":" + entry.getKey() + ":" + entry.getValue());
            }
        }
    }
}
