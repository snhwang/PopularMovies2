package com.snh.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.API_KEY;
    private String listChoice;
    static final String STATE_LIST_CHOICE = "listChoice";
    static final String STATE_LIST_PAGE = "page";
    static final String STATE_SCROLL_POSITION = "scrollPosition";
    private int page;
    private int gridIndex;

    List<Movie> movies = new ArrayList<>();
    List<Movie> favorites = new ArrayList<>();

    private static final int FAVORITES_LOADER = 22;

    private SharedPreferences prefs;

    @BindView(R.id.spinner1) Spinner spinner1;
    @BindView(R.id.gridView) GridView gridView;
    @BindView(R.id.pageNum) TextView pageNum;
    @BindView(R.id.btnFirst) Button btnFirst;
    @BindView(R.id.btnNext) Button btnNext;
    @BindView(R.id.btnPrevious) Button btnPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        addListenersToPageButtons();
        addListenerToListSelector();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(STATE_LIST_CHOICE, listChoice);
        savedInstanceState.putInt(STATE_LIST_PAGE, page);
        savedInstanceState.putInt(STATE_SCROLL_POSITION, gridView.getFirstVisiblePosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listChoice = savedInstanceState.getString(STATE_LIST_CHOICE);
        page = savedInstanceState.getInt(STATE_LIST_PAGE, 1);
        pageNum.setText(String.valueOf(page));        // start loader to read stored favorites
        getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, loaderCallbacks);
        gridIndex = savedInstanceState.getInt(STATE_SCROLL_POSITION, 0);
        gridView.setSelection(gridIndex);
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STATE_LIST_CHOICE, listChoice);
        editor.putInt(STATE_LIST_PAGE, page);
        editor.putInt(STATE_SCROLL_POSITION, gridView.getFirstVisiblePosition());
        editor.clear();
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        listChoice = prefs.getString(STATE_LIST_CHOICE, "Popular");

        switch(listChoice) {
            case "Popular":
                spinner1.setSelection(0);
                break;
            case "Top Rated":
                spinner1.setSelection(1);
                break;
            case "Favorites":
                spinner1.setSelection(2);
                break;
            default:
                spinner1.setSelection(0);
        }

        page = prefs.getInt(STATE_LIST_PAGE, 1);
        pageNum.setText(String.valueOf(page));
        getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, loaderCallbacks);
        gridIndex = prefs.getInt(STATE_SCROLL_POSITION, 0);
        gridView.setSelection(gridIndex);
    }

    public void addListenerToListSelector() {
        spinner1.setOnItemSelectedListener(new listSelectorListener());
    }

    public class listSelectorListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String temp = parent.getItemAtPosition(pos).toString();
            if (!temp.equals(listChoice)) {
                gridIndex = 0;
                page = 1;
            }
            listChoice = temp;
            if ("Favorites".equals(listChoice)) {
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, loaderCallbacks);
            }
            else {
                updateGrid();
                gridView.setSelection(gridIndex);
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    // Listeners for the the "Next" and "Previous" page buttons
    public void addListenersToPageButtons() {
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 1;
                updateGrid();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                page++;
                if (page > 50) page = 50;
                updateGrid();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                page--;
                if (page < 1) page = 1;
                updateGrid();
            }
        });
    }

    // Update the displayed grid of movie posters
    private void updateGrid() {
        if (!"Favorites".equals(listChoice)) {
            String listName;
            if ("Top Rated".equals(listChoice)) {
                listName = "top_rated";
            }
            else {
                listName = "popular";
            }
            pageNum.setText(String.valueOf(page));

            if (Utils.isOnline(MainActivity.this)) {
                String url = getString(R.string.apiListUrl, listName, page, API_KEY);
                try {
                    getMovieData(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Store the data retrieved from the movie database in a Movie arraylist
    // and display the movie posters in the grid.
    public void processMovieData(String response) {
        JSONObject data=null;
        try {
            data = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray posters = data.optJSONArray("results");
        movies.clear();
        for (int i = 0; posters != null && i < posters.length(); i++) {
            try {
                movies.add(new Movie(MainActivity.this, posters.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        gridView.setAdapter(new MoviesToGridImageAdapter(this, movies));
        gridView.setSelection(gridIndex);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Class destinationActivity = Detail.class;
                Intent showDetailActivity = new Intent(MainActivity.this, destinationActivity);
                Movie movie = movies.get(position);
                if (checkIfFavorited(movie)) {
                    movie.favorited = true;
                }
                else {
                    movie.favorited = false;
                }
                showDetailActivity.putExtra("MOVIE_DATA", movie);
                startActivity(showDetailActivity);
            }
        });
    }

/*
Get the movie lists from the movie database. A single page (currently 20 movies) is obtained.
Adapted from:
https://www.journaldev.com/13629/okhttp-android-example-tutorial#synchronous-vs-asynchronous-calls
*/
    void getMovieData(String url) throws IOException {
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processMovieData(myResponse);
                    }
                });
            }
        });
    }

    private LoaderManager.LoaderCallbacks<Cursor>
           loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == FAVORITES_LOADER) {
                return new CursorLoader(
                        getApplicationContext(),
                        FavoriteContentProvider.URI_FAVORITES,
                        null, null, null, null);
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor movieCursor) {
            switch (loader.getId()) {
                case FAVORITES_LOADER:
                    favorites = new ArrayList<>();
                    movieCursor.moveToFirst();
                    for (int i = 0; i < movieCursor.getCount(); i++) {
                        ContentValues values = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(movieCursor, values);
                        Movie movie = Movie.fromContentValues(values);
                        movie.favorited = true;
                        favorites.add(Movie.fromContentValues(values));;
                        movieCursor.moveToNext();
                    }
                    if ("Favorites".equals(listChoice)) {
                        gridView.setAdapter(new MoviesToGridImageAdapter(MainActivity.this, favorites));
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v,
                                                    int position, long id) {
                                Class destinationActivity = Detail.class;
                                Intent showDetailActivity = new Intent(MainActivity.this, destinationActivity);
                                Movie movie = favorites.get(position);
                                movie.favorited = true;
                                showDetailActivity.putExtra("MOVIE_DATA", movie);
                                startActivity(showDetailActivity);
                            }
                        });
                    }
                    gridView.setSelection(gridIndex);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case FAVORITES_LOADER:
                    favorites = new ArrayList<>();
                    break;
            }
        }
    };

    // Check if the movie is on the favorites list
    Boolean checkIfFavorited(Movie movie) {
        for (int i = 0; i < favorites.size(); i++) {
            if (movie.id == favorites.get(i).id) {
                return true;
            }
        }
        return false;
    }
}
