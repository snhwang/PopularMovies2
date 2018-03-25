package com.snh.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by snhwa on 12/11/2017.
 */

public class Review implements Parcelable {
    String id;
    String author;
    String content;
    Boolean showContent;

    public Review(JSONObject reviewData) {
        try {
            id = reviewData.getString("id");
            author = reviewData.getString("author");
            content = reviewData.getString("content");
            showContent = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.author);
        dest.writeString(this.content);
    }

    protected Review(Parcel in) {
        this.id = in.readString();
        this.author = in.readString();
        this.content = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
