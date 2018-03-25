package com.snh.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by snhwa on 12/30/2017.
 */

/*
Adapted from the following:
Udacity course materials
https://developer.android.com/training/data-storage/sqlite.html
https://www.tutorialspoint.com/android/android_sqlite_database.htm
https://www.androidauthority.com/use-sqlite-store-data-app-599743/
*/
public class FavoritesSqlHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "favorites.db";

    public FavoritesSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


/*
    public static final String TABLE_NAME = "favorites";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_POSTER_URL = "posterUrl";
    public static final String COLUMN_NAME_RELEASE_DATE = "releaseDate";
    public static final String COLUMN_NAME_VOTE_AVERAGE = "voteAverage";
    public static final String COLUMN_NAME_OVERVIEW = "overview";
*/

    //Table contents
    public class FavoritesEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER_URL = "posterUrl";
        public static final String COLUMN_NAME_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_NAME_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_NAME_OVERVIEW = "overview";

    }

/*
I used the autoincrementing _ID in addition to the ID from the movie database.
I don't have a specific use for the redundant id numbers a this time but maybe
in the future. For example, adding movies from other sources.
*/
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                    FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FavoritesEntry.COLUMN_NAME_ID + " INTEGER," +
                    FavoritesEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    FavoritesEntry.COLUMN_NAME_POSTER_URL + " TEXT," +
                    FavoritesEntry.COLUMN_NAME_RELEASE_DATE + " TEXT," +
                    FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE + " TEXT," +
                    FavoritesEntry.COLUMN_NAME_OVERVIEW + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME;

    public void onCreate(SQLiteDatabase db) {
        Log.d("FavoriesSqlHelper", SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from contacts where id=" +
                id + "", null);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FavoritesEntry.TABLE_NAME);
        return numRows;
    }

    public boolean updateFavorite (
            long id,
            String title,
            String posterUrl,
            String releaseDate,
            String voteAverage,
            String overview) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FavoritesEntry.COLUMN_NAME_ID, id);
        contentValues.put(FavoritesEntry.COLUMN_NAME_TITLE, title);
        contentValues.put(FavoritesEntry.COLUMN_NAME_POSTER_URL, posterUrl);
        contentValues.put(FavoritesEntry.COLUMN_NAME_RELEASE_DATE, releaseDate);
        contentValues.put(FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE, voteAverage);
        contentValues.put(FavoritesEntry.COLUMN_NAME_OVERVIEW, overview);
        db.update(
                FavoritesEntry.TABLE_NAME,
                contentValues,
                "id = ? ",
                new String[] {String.valueOf(id)});
        return true;
    }

    public Integer deleteContact (long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FavoritesEntry.TABLE_NAME,
                "id = ? ",
                new String[] {String.valueOf(id)});
    }

    public ArrayList<Movie> getAllFavorites() {
        ArrayList<Movie> favorites = new ArrayList<Movie>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor movieCursor =
                db.rawQuery(
                        "select * from " + FavoritesEntry.TABLE_NAME,
                        null );
        movieCursor.moveToFirst();
        for (int i = 0; i < movieCursor.getCount(); i++) {
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(movieCursor, values);
            Movie movie = Movie.fromContentValues(values);
            movie.favorited = true;
            favorites.add(Movie.fromContentValues(values));;
            movieCursor.moveToNext();
        }
        return favorites;
    }

}