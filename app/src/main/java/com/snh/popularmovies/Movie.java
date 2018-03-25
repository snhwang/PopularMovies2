package com.snh.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by snhwa on 12/11/2017.
 */

public class Movie implements Parcelable {

    public static final String TABLE_NAME = "favorites";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_TITLE = "title";

//    @PrimaryKey
    long id;

    String title;

    String posterUrl;

    String releaseDate;

    String overview;

    String voteAverage;

    Boolean favorited;

    public Movie(long id, String title, String posterUrl, String releaseDate, String overview, String voteAverage) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.favorited = false;
    }

    public Movie(Context mContext, JSONObject movieData) {
        try {
            posterUrl = mContext.getString(R.string.posterURL, movieData.getString("poster_path"));
            id = Long.valueOf(movieData.getString("id"));
            title = movieData.getString("title");
            releaseDate = movieData.getString("release_date");
            voteAverage = movieData.getString("vote_average");
            overview = movieData.getString("overview");
            favorited = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Movie(Cursor cursor) {
        try {
            int idCol = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int titleCol = cursor.getColumnIndexOrThrow("title");
            int posterUrlCol = cursor.getColumnIndexOrThrow("posterUrl");
            int releaseDateCol = cursor.getColumnIndexOrThrow("title");
            int voteAverageCol = cursor.getColumnIndexOrThrow("title");
            int overviewCol = cursor.getColumnIndexOrThrow("title");
            id = cursor.getLong(idCol);
            title = cursor.getString(titleCol);
            posterUrl = cursor.getString(posterUrlCol);
            releaseDate = cursor.getString(releaseDateCol);
            voteAverage = cursor.getString(voteAverageCol);
            overview = cursor.getString(overviewCol);
            favorited = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Movie() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(posterUrl);
        dest.writeString(releaseDate);
        dest.writeString(overview);
        dest.writeString(voteAverage);
        if (favorited != null) dest.writeString(favorited.toString());
    }

    protected Movie(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.posterUrl = in.readString();
        this.releaseDate = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readString();
        this.favorited = Boolean.valueOf(in.readString());
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    // Create a Movie from ContentValues
    public static Movie fromContentValues(ContentValues values) {
        final Movie movie = new Movie();
        if (values.containsKey(COLUMN_ID)) {
            movie.id = values.getAsLong(COLUMN_ID);
        }
        if (values.containsKey("title")) {
            movie.title = values.getAsString(COLUMN_TITLE);
        }
        if (values.containsKey("posterUrl")) {
            movie.posterUrl = values.getAsString("posterUrl");
        }
        if (values.containsKey("releaseDate")) {
            movie.releaseDate = values.getAsString("releaseDate");
        }
        if (values.containsKey("overview")) {
            movie.overview = values.getAsString("overview");
        }
        if (values.containsKey("voteAverage")) {
            movie.voteAverage = values.getAsString("voteAverage");
        }
        return movie;
    }

    // Create a Movie from ContentValues
    public static ContentValues toContentValues(Movie movie) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.id);
        values.put(COLUMN_TITLE, movie.title);
        values.put("posterUrl", movie.posterUrl);
        values.put("releaseDate", movie.releaseDate);
        values.put("overview", movie.overview);
        values.put("voteAverage", movie.voteAverage);

        return values;
    }

}
