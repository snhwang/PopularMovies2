package com.snh.popularmovies;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by snhwa on 12/15/2017.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private List<Review> reviews;
    private OnReviewClickListener onReviewClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView author, content;

        public ViewHolder(View view) {
            super(view);

            author = view.findViewById(R.id.reviewAuthor);
            content = view.findViewById(R.id.reviewContent);
        }
    }

    public ReviewsAdapter(List<Review> reviews)
    {
        this.reviews = reviews;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        ScrollView v = (ScrollView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_detail, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Review review = reviews.get(position);
        holder.author.setText("Author: " + review.author);
        if (review.showContent) {
            holder.content.setText(review.content);
        }
        else {
            holder.content.setText("");
        }
        final TextView tv;
        final Review r = review;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onReviewClickListener == null) {
                }
                onReviewClickListener.onReviewClick(review);
            }
        };
        holder.author.setOnClickListener(listener);
        holder.content.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public OnReviewClickListener getOnReviewClickListener() {
        return onReviewClickListener;
    }

    public void setOnReviewClickListener(OnReviewClickListener onReviewClickListener) {
        this.onReviewClickListener = onReviewClickListener;
    }

}


