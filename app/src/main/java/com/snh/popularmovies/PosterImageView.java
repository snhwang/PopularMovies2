package com.snh.popularmovies;
import android.content.Context;
import android.util.AttributeSet;


/**
 * Created by snhwa on 12/10/2017.
 */

    /** ImageView with aspect ratio of the poster images at The Movie DB. */
    public class PosterImageView extends android.support.v7.widget.AppCompatImageView {
        public PosterImageView(Context context) {
            super(context);
        }

        public PosterImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int width = getMeasuredWidth();
            setMeasuredDimension(width, (int) (1.5 * width));
        }
    }
