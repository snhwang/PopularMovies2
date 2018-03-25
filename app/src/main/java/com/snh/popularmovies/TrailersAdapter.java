package com.snh.popularmovies;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by snhwa on 12/15/2017.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.ViewHolder> {
    private List<Trailer> trailers;
    private OnTrailerClickListener onTrailerClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView playButton;

        public ViewHolder(View view) {
            super(view);
            playButton = view.findViewById(R.id.play_button);
            name = view.findViewById(R.id.trailerName);
        }
    }

    public TrailersAdapter(List<Trailer> trailers)
    {
        this.trailers = trailers;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TrailersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_detail, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Trailer trailer = trailers.get(position);
        holder.name.setText(trailer.name);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrailerClickListener.onTrailerClick(trailer);
            }
        };
        holder.playButton.setOnClickListener(listener);
        holder.name.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public OnTrailerClickListener getOnTrailerClickListener() {
        return onTrailerClickListener;
    }

    public void setOnTrailerClickListener(OnTrailerClickListener onTrailerClickListener) {
        this.onTrailerClickListener = onTrailerClickListener;
    }
}
