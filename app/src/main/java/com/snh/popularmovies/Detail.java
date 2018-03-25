package com.snh.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


// Displays the details page for a single movie. Includes trailers are reviews.
// The trailers will be played on a browser if the YouTube app is not present.
public class Detail extends AppCompatActivity {

    static final String STATE_REVIEWS = "reviews";
    static final String STATE_TRAILERS = "trailers";
    Movie movie;
    List<Review> reviews = new ArrayList<>();
    ReviewsAdapter reviewAdapter;

    List<Trailer> trailers = new ArrayList<>();
    TrailersAdapter trailerAdapter;

    @BindView(R.id.image1) ImageView imageView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.id) TextView id;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.releaseDate) TextView releaseDate;
    @BindView(R.id.voteAverage) TextView voteAverage;
    @BindView(R.id.overview) TextView overview;
    @BindView(R.id.reviewsRecycler) RecyclerView reviewsView;
    @BindView(R.id.trailersRecycler) RecyclerView trailersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final String API_KEY = BuildConfig.API_KEY;
        ButterKnife.bind(this);

        movie = getIntent().getParcelableExtra("MOVIE_DATA");
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        if (savedInstanceState == null && Utils.isOnline(Detail.this)) {
            String reviewsUrl = getString(R.string.reviewsListUrl, String.valueOf(movie.id), API_KEY);
            String trailersUrl = getString(R.string.trailersListUrl, String.valueOf(movie.id), API_KEY);
            try {
                getReviewsData(reviewsUrl);
                getTrailersData(trailersUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (!Utils.isOnline(Detail.this)){
            Toast.makeText(Detail.this, "No Internet", Toast.LENGTH_LONG).show();
        }

        String url = movie.posterUrl;
        if (url != null) {
            Picasso.with(getBaseContext())
                    .load(url)
                    .into(imageView);
        }

        if (movie.favorited) {
            fab.setImageResource(android.R.drawable.btn_star_big_on);
            movie.favorited = true;
        }
        else {
            fab.setImageResource(android.R.drawable.btn_star_big_off);
            movie.favorited = false;
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.fab) {
                    fab.setImageResource(android.R.drawable.btn_star_big_on);
                    if (!movie.favorited) {
                        movie.favorited = true;
                        fab.setImageResource(android.R.drawable.btn_star_big_on);
                        FavoritesHandler inserter = new FavoritesHandler(getContentResolver());
                        inserter.insert(movie);
                    }
                    else {
                        movie.favorited = false;
                        fab.setImageResource(android.R.drawable.btn_star_big_off);
                        FavoritesHandler remover = new FavoritesHandler(getContentResolver());
                        remover.delete(movie);
                    }
                }
            }
        });

        id.setText("ID: " + String.valueOf(movie.id));
        title.setText("Title: " + movie.title);
        releaseDate.setText("Release Date: " + movie.releaseDate);
        voteAverage.setText("Vote Average: " + movie.voteAverage);
        overview.setText(movie.overview);
        displayReviews();
        displayTrailers();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(STATE_REVIEWS, new ArrayList<>(reviews));
        savedInstanceState.putParcelableArrayList(STATE_TRAILERS, new ArrayList<>(trailers));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        reviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
        trailers = savedInstanceState.getParcelableArrayList(STATE_TRAILERS);
        displayReviews();
        displayTrailers();
    }

    void displayReviews() {
        reviewAdapter = new ReviewsAdapter(reviews);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(Detail.this);
        reviewsView.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(reviewsView.getContext(),
                mLayoutManager.getOrientation());
        reviewsView.addItemDecoration(mDividerItemDecoration);
        reviewsView.setItemAnimator(new DefaultItemAnimator());
        reviewsView.setAdapter(reviewAdapter);
        reviewAdapter.setOnReviewClickListener(new OnReviewClickListener() {
            @Override
            public void onReviewClick(Review review) {
                if (review.showContent) {
                    review.showContent = false;
                }
                else {
                    review.showContent = true;
                }
                reviewAdapter.notifyDataSetChanged();
            }
        });
    }

    void displayTrailers() {
        trailerAdapter = new TrailersAdapter(trailers);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(Detail.this);
        trailersView.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(trailersView.getContext(),
                mLayoutManager.getOrientation());
        trailersView.addItemDecoration(mDividerItemDecoration);


        trailersView.setItemAnimator(new DefaultItemAnimator());
        trailersView.setAdapter(trailerAdapter);
        trailerAdapter.setOnTrailerClickListener(new OnTrailerClickListener() {
            @Override
            public void onTrailerClick(Trailer trailer) {
                playYouTubeVideo(Detail.this, trailer.key);
            }
        });
    }

    void getReviewsData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                Detail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processReviewsData(myResponse);
                    }
                });
            }
        });
    }

    public void processReviewsData(String response) {
        JSONObject data = null;
        try {
            data = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray reviewData = data.optJSONArray("results");
        for (int i = 0; i < reviewData.length(); i++) {
            try {
                reviews.add(new Review(reviewData.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        displayReviews();
    }

    void getTrailersData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                Detail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processTrailersData(myResponse);
                    }
                });
            }
        });
    }

    public void processTrailersData(String response) {
        JSONObject data = null;
        try {
            data = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray trailerData = data.optJSONArray("results");
        for (int i = 0; i < trailerData.length(); i++) {
            try {
                trailers.add(new Trailer(trailerData.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        displayTrailers();
    }

/*
Adapted from:
    https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
*/
    public void playYouTubeVideo(Context context, String id){
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id)));
        } catch (ActivityNotFoundException ex) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id)));
        }
    }

    private class AddToFavorites extends AsyncTask<String, Integer, Void> {
        protected Void doInBackground(String... params) {
            getContentResolver().insert(
                    FavoriteContentProvider.URI_FAVORITES,
                    Movie.toContentValues(movie));
            return null;
        }
        protected void onPostExecute(Void dummy) {
            Toast.makeText(Detail.this, "Added to favorites", Toast.LENGTH_LONG).show();
        }
    }

    private class RemoveFromFavorites extends AsyncTask<String, Integer, Void> {
        protected Void doInBackground(String... params) {
            getContentResolver().delete(
                    ContentUris.withAppendedId(
                            FavoriteContentProvider.URI_FAVORITES, movie.id),
                    null,
                    null);
/*
            FavoritesDatabase
                    .getInstance(Detail.this)
                    .getMovieDao()
                    .deleteById(movie.id);
*/
            return null;
        }
        protected void onPostExecute(Void dummy) {
            Toast.makeText(Detail.this, "Removed from favorites", Toast.LENGTH_LONG).show();
        }
    }

    private class FavoritesHandler extends AsyncQueryHandler {

        public FavoritesHandler(ContentResolver cr) {
            super(cr);
        }

        public void insert(Movie movie) {
            startInsert(
                    0,
                    null,
                    FavoriteContentProvider.URI_FAVORITES,
                    Movie.toContentValues(movie));
        }

        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            Toast.makeText(Detail.this, "Added to Favorites", Toast.LENGTH_LONG).show();
        }

        public void delete(Movie movie) {
            startDelete(
                    0,
                    null,
                    ContentUris.withAppendedId(
                            FavoriteContentProvider.URI_FAVORITES, movie.id), null,
                    null
            );
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            Toast.makeText(Detail.this, "Removed from Favorites", Toast.LENGTH_LONG).show();
        }
    }
}

