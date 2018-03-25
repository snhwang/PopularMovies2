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
import android.util.Log;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.API_KEY;
    private Spinner spinner1, spinner2;
    private Button btnNext, btnPrevious, btnFirst;
    private String listChoice;
    static final String STATE_LIST_CHOICE = "listChoice";
    static final String STATE_LIST_PAGE = "page";
    static final String STATE_SCROLL_POSITION = "scrollPosition";
    private int page;
    static final String STATE_MAIN_LAYOUT = "mainLayout";
    List<Movie> movies = new ArrayList<>();
    List<Movie> favorites = new ArrayList<>();

    private static final int FAVORITES_LOADER = 22;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "entry");
        setContentView(R.layout.activity_main);
        addListenersToPageButtons();
        addListenerToListSelector();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(STATE_LIST_CHOICE, listChoice);
        GridView gridView = findViewById(R.id.gridView);
        Log.d("nSaveInstanceState", String.valueOf(gridView.getFirstVisiblePosition()));
        savedInstanceState.putInt(STATE_SCROLL_POSITION, gridView.getFirstVisiblePosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listChoice = savedInstanceState.getString(STATE_LIST_CHOICE);
        // start loader to read stored favorites
        getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, loaderCallbacks);
        int index = savedInstanceState.getInt(STATE_SCROLL_POSITION);
        Log.d("onRestore index", String.valueOf(index));
        GridView gridView = findViewById(R.id.gridView);
        gridView.smoothScrollToPositionFromTop(10, 0, 15);
        gridView.setSelection(index);
        Log.d("index after scroll", String.valueOf(gridView.getFirstVisiblePosition()));
//        updateGrid();
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STATE_LIST_CHOICE, listChoice);
        editor.putInt(STATE_LIST_PAGE, page);
        GridView gridView = findViewById(R.id.gridView);
        Log.d("onPause", String.valueOf(gridView.getFirstVisiblePosition()));
        editor.putInt(STATE_SCROLL_POSITION, gridView.getFirstVisiblePosition());
        editor.clear();
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        listChoice = prefs.getString(STATE_LIST_CHOICE, "Popular");
        spinner1 = findViewById(R.id.spinner1);
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
        TextView pageNum = findViewById(R.id.pageNum);
        pageNum.setText(String.valueOf(page));

        Log.d("onResume", "before load");
        getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, loaderCallbacks);
        Log.d("onResume", "after load");

        //if (!"Favorites".equals(listChoice)) updateGrid();
        Log.d("onResume", "end");
        int index = prefs.getInt(STATE_SCROLL_POSITION, 0);
        Log.d("onResume index", String.valueOf(index));
        GridView gridView = findViewById(R.id.gridView);
        gridView.smoothScrollToPositionFromTop(10,0, 15);
        gridView.setSelection(index);
        Log.d("index after scroll", String.valueOf(gridView.getFirstVisiblePosition()));

    }

    public void addListenerToListSelector() {
        spinner1 = findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new listSelectorListener());
    }

    public class listSelectorListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            listChoice = parent.getItemAtPosition(pos).toString();
            if ("Favorites".equals(listChoice)) {
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, loaderCallbacks);
            }
            else updateGrid();
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    // Listeners for the the "Next" and "Previous" page buttons
    public void addListenersToPageButtons() {
        btnFirst = findViewById(R.id.btnFirst);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

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
        Log.d("updateGrid", "entry");
        if (!"Favorites".equals(listChoice)) {
            String listName;
            if ("Top Rated".equals(listChoice)) {
                listName = "top_rated";
            }
            else {
                listName = "popular";
            }
            TextView pageNum = findViewById(R.id.pageNum);
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
        Log.d("processMovieData", "entry");
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
                Log.d("processMovieData", movies.get(i).title);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        GridView gridview = findViewById(R.id.gridView);
        gridview.setAdapter(new MoviesToGridImageAdapter(this, movies));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        Log.d("getMovieData", "entry");
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
                        Log.d("getMovieData", "run");
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
            Log.d("LoaderManager", "onCreateLoader entry");
            if (id == FAVORITES_LOADER) {
                Log.d("LoaderManager", "onCreateLoader");
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
                        GridView gridview = findViewById(R.id.gridView);
                        gridview.setAdapter(new MoviesToGridImageAdapter(MainActivity.this, favorites));
                        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
