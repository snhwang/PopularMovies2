package com.snh.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

/**
 * Created by snhwa on 12/8/2017.
 * Adapted from https://developer.android.com/guide/topics/ui/layout/gridview.html
 * and https://github.com/square/picasso/blob/master/picasso-sample/src/main/java/com/example/picasso/SampleGridViewAdapter.java
 */

public class MoviesToGridImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<Movie> movies = new ArrayList<>();

    public MoviesToGridImageAdapter(Context mContext, List<Movie> movies) {
        this.mContext = mContext;
        this.movies = movies;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        PosterImageView view = (PosterImageView) convertView;
        if (view == null) {
            view = new PosterImageView(mContext);
            view.setScaleType(CENTER_CROP);
        }
        String url = getItem(position);
        Picasso.with(mContext)
                .load(url)
                .error(R.drawable.ic_no_image)
                .fit()
                .into(view);


        return view;
    }

    @Override public int getCount() {
        return movies.size();
    }

    @Override public String getItem(int position) {
        return movies.get(position).posterUrl;
    }

    @Override public long getItemId(int position) {
        return position;
    }
}