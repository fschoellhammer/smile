package com.jennyabrahamson.smile;

import android.view.View;
import android.widget.Adapter;
import org.lucasr.smoothie.SimpleItemLoader;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class FeedImageLoader extends SimpleItemLoader<Object, CacheableBitmapDrawable> {
    final BitmapLruCache mCache;

    public FeedImageLoader(BitmapLruCache cache) {
        mCache = cache;
    }

    @Override
    public CacheableBitmapDrawable loadItemFromMemory(Object item) {
        if (item instanceof StoryItem) {
            return mCache.getFromMemoryCache(((StoryItem) item).getImageUrl());
        }
        return null;
    }

    @Override
    public Object getItemParams(Adapter adapter, int position) {
        return adapter.getItem(position);
    }

    @Override
    public CacheableBitmapDrawable loadItem(Object item) {
        if (item instanceof StoryItem) {
            String url = ((StoryItem) item).getImageUrl();
            CacheableBitmapDrawable wrapper = mCache.get(url);
            if (wrapper == null) {
                wrapper = mCache.put(url, HttpHelper.loadImage(url));
            }

            return wrapper;
        }
        return null;
    }

    @Override
    public void displayItem(View itemView, CacheableBitmapDrawable result, boolean fromMemory) {
        FeedObject.ViewHolder holder = (FeedObject.ViewHolder) itemView.getTag();

        if (result == null || holder == null) {
            return;
        }

        holder.image.setImageDrawable(result);
    }
}
