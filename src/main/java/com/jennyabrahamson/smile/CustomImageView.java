package com.jennyabrahamson.smile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * CustomImageView simply forces an ImageView (based on a bitmap) to take the full available width
 * and to set height based on maintaining original aspect ratio given the full width.
 */
public class CustomImageView extends ImageView {

    private Bitmap bitmap;

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        this.bitmap = bitmap;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable instanceof BitmapDrawable) {
            this.bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (bitmap != null) {
            float imageRatio = (float) bitmap.getHeight() / bitmap.getWidth();
            setMeasuredDimension(getMeasuredWidth(), Math.round(getMeasuredWidth() * imageRatio));
        }
    }
}
