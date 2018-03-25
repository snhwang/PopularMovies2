package com.snh.popularmovies;

/**
 * Created by snhwa on 12/17/2017.
 * Adapted from Udacity courseware and from the following:
 *      https://github.com/googlesamples/
 *      android-architecture-components/tree/master/PersistenceContentProviderSample
 * and
 *      https://developer.android.com/guide/topics/providers/content-provider-creating.html
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import static com.snh.popularmovies.FavoritesSqlHelper.FavoritesEntry.COLUMN_NAME_TITLE;
import static com.snh.popularmovies.FavoritesSqlHelper.FavoritesEntry.TABLE_NAME;

public class FavoriteContentProvider extends ContentProvider {

    private SQLiteDatabase db;
    private FavoritesSqlHelper dbHelper;


    // Content authority
    public static final String AUTHORITY = "com.snh.popularmovies";

    // URI for favorite movies table.
    public static final Uri URI_FAVORITES =
            Uri.parse("content://" + AUTHORITY + "/" + Movie.TABLE_NAME);

    //match code for multiple rows in the favorites table.
    private static final int CODE_MULTIPLE_ROWS = 1;

    //match code for a single row of the favorites table.
    private static final int CODE_SINGLE_ROW = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        // Sets the integer code for actions involving multiple rows of the table.
        uriMatcher.addURI(AUTHORITY, Movie.TABLE_NAME, CODE_MULTIPLE_ROWS);
        // Sets the integer code for actions involving a single row of the table.
        uriMatcher.addURI(AUTHORITY, Movie.TABLE_NAME + "/#", CODE_SINGLE_ROW);
    }

    //ContentProvider requires 6 implemented methods:
    // query(), insert(), update(), delete(), getType(), onCreate().

    // Required implementation of ContentProvider.query
    public Cursor query(
        Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        db = dbHelper.getWritableDatabase();

        switch(uriMatcher.match(uri)) {

            case CODE_MULTIPLE_ROWS:
                Context context = getContext();
                if (context == null) {
                    return null;
                }
                if (sortOrder == null || sortOrder == ""){
                    sortOrder = COLUMN_NAME_TITLE;
                }

                Cursor c = qb.query(db,	projection,	selection,
                        selectionArgs,null, null, sortOrder);
                /**
                 * register to watch a content URI for changes
                 */
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;

//                FavoritesDao movieDao = FavoritesDatabase.getInstance(context).getMovieDao();
//                Cursor cursor = movieDao.getMovies();
//                cursor.setNotificationUri(context.getContentResolver(), uri);
//                cursor.moveToFirst();
//                for (int i = 0; i < cursor.getCount(); i++) {
//                    cursor.moveToNext();
//                }
//                return cursor;
            case CODE_SINGLE_ROW:
                context = getContext();
                if (context == null) {
                    return null;
                }
                qb.appendWhere( BaseColumns._ID + "=" + uri.getPathSegments().get(1));
                c = qb.query(db, projection, selection,
                        selectionArgs,null, null, sortOrder);
                /**
                 * register to watch a content URI for changes
                 */
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    // Required implementation of ContentProvider.insert()
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case CODE_MULTIPLE_ROWS:
                final Context context = getContext();
                if (context == null) {
                    return null;
                }
                long id = db.insert(TABLE_NAME, "", values);
                if (id > 0) {
                    Uri _uri = ContentUris.withAppendedId(URI_FAVORITES, id);
                    context.getContentResolver().notifyChange(_uri, null);
                    return _uri;
                }

                throw new IllegalArgumentException("Failed to add a record into " + uri);
            case CODE_SINGLE_ROW:
                throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    // Required implementation of ContentProvider.update(). Returns number of table rows updated.
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case CODE_MULTIPLE_ROWS:
                throw new IllegalArgumentException
                        ("Invalid URI, please specify a single ID: " + uri);
            case CODE_SINGLE_ROW:
                final Context context = getContext();
                if (context == null) {
                    return 0;
                }
                final int count = db.update(
                        TABLE_NAME,
                        values,
                        BaseColumns._ID + " = " +
                                uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ?
                                        " AND (" +selection + ')' : ""),
                        selectionArgs);
                context.getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    // Required implementation of ContentProvider.delete()
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case CODE_MULTIPLE_ROWS:
                throw new IllegalArgumentException
                        ("Invalid URI, please specify a single ID: " + uri);
            case CODE_SINGLE_ROW:
                final Context context = getContext();
                if (context == null) {
                    return 0;
                }

                String id = uri.getPathSegments().get(1);
                final int count = db.delete(
                        TABLE_NAME,
                        BaseColumns._ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ?
                                        " AND (" + selection + ')' : ""),
                                selectionArgs);

                context.getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    // Required implementation of ContentProvider.getType()
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_MULTIPLE_ROWS:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + Movie.TABLE_NAME;
            case CODE_SINGLE_ROW:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + Movie.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    // Required implementation of ContentProvider.onCreate()
    public boolean onCreate() {
        dbHelper = new FavoritesSqlHelper(getContext());
        return true;
    }
}

