package com.jennyabrahamson.smile;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class StoryItem {
    private String title;
    private String imageUrlString;
    private int votes;
    private Calendar date;
    private boolean isAnimated;
    private int imageWidth;
    private int imageHeight;

    private boolean isLiked;

    private StoryItem() {}

    public static StoryItem initWithJson(JSONObject data)
            throws JSONException, MalformedURLException {
        StoryItem item = new StoryItem();
        item.title = sanitizeString(data.optString("title"));
        item.imageUrlString = data.optString("image_url2");
        item.votes = data.optInt("votes");

        item.imageHeight = data.optInt("image_height");
        if (item.imageHeight == 0) {
            item.imageHeight = 200;
        }

        item.imageWidth = data.optInt("image_width");
        if (item.imageWidth == 0) {
            item.imageWidth = 360;
        }

        item.isAnimated = data.optBoolean("is_animated");
        item.isLiked = data.optBoolean("is_liked");

        item.date = new GregorianCalendar();
        item.date.setTimeInMillis(1000 * data.optLong("posted_date"));

        return item;
    }

    public void setLiked(boolean liked) {
        this.votes += liked ? 1 : -1;
        this.isLiked = liked;
    }

    private static String sanitizeString(String input) {
        if (input == null || input.length() <= 0) {
            return "";
        }

        input = input.replaceAll("&amp", "&");
        input = input.replaceAll("&gt", ">");
        input = input.replaceAll("&lt", "<");

        return input;
    }

    public String getTitle() { return title; }

    public boolean getIsLiked() { return isLiked; }

    public int getVotes() { return votes; }

    public Calendar getPostedDate() { return date; }

    public String getImageUrl() { return imageUrlString; }

    public boolean isAnimated() { return isAnimated; }
}
