package com.example.manik.ytsapi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    StringBuilder newRequest = new StringBuilder(YTS_BASE_URL);

    private static final String YTS_BASE_URL = "https://yts.am/api/v2/list_movies.json";

    private static final String YTS_QUERY_BEGINNER = "?";

    private static final String YTS_QUERY_SEPARATOR = "&";

    private static final String YTS_DEFAULT_LIMIT = "20";

    private static final String YTS_MAX_LIMIT = "50";

    private static final String YTS_DEFAULT_PAGE_NUMBER = "1";

    private final static String YTS_REQUEST_FILTER_LIMIT = "limit=";

    private final static String YTS_REQUEST_FILTER_QUALITY = "quality=";

    private final static String YTS_REQUEST_FILTER_RATING = "minimum_rating=";

    private final static String YTS_REQUEST_FILTER_GENRE = "genre=";

    private final static String YTS_REQUEST_FILTER_SORT = "sort_by=";

    private final static String YTS_REQUEST_FILTER_ORDER = "order_by=";

    private final static String YTS_REQUEST_SEARCH = "query_term=";

    private final static String YTS_REQUEST_FILTER_PAGE_NUM = "page=";

    boolean isQuality = false;
    boolean isGenre = false;
    boolean isRating = false;
    boolean isSort = false;
    boolean isOrder = false;
    boolean isLimit = false;
    boolean isSearch = false;

    int qualityItem = 0;
    int genreItem = 0;
    int ratingItem = 0;
    int sortItem = 0;
    int orderItem = 0;
    int limit = 20;

    String ratingFilter = null;
    String qualityFilter = null;
    String genreFilter = null;
    String sortFilter = null;
    String orderFilter = null;
    String searchQueryEntered = null;

    private View emptyView;

    private MovieAdapter movieAdapter;

    private static final int MOVIE_LOADER_ID = 1;

    private TextView emptyTextView;
    private ImageView emptyImageView;

    private String DETAILS_REQUEST_URL = null;
    private String DETAILS_REQUEST_PREFIX = "https://yts.am/api/v2/movie_details.json?with_cast=true&movie_id=";

    public final static String MOVIE_DETAILS = "com.example.manik.ytsapi.movie_details";

    private DrawerLayout myDrawer;
    private ActionBarDrawerToggle myDrawerToggle;

    private ListView movieList;

    private View pageBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieList = findViewById(R.id.list);

        movieAdapter = new MovieAdapter(this, new ArrayList<Movie>());

        movieList.setAdapter(movieAdapter);

        emptyView = findViewById(R.id.empty_layout);

        pageBottom = findViewById(R.id.page_bottom);
        pageBottom.setVisibility(View.GONE);

        totalPages = findViewById(R.id.total_pages_view);
        currentPage = findViewById(R.id.current_page_view);

        currentPage.setText(YTS_DEFAULT_PAGE_NUMBER);

        int movieLimit;

        if (isLimit) {
            movieLimit = limit;
        } else {
            movieLimit = Integer.parseInt(YTS_DEFAULT_LIMIT);
        }

        emptyTextView = emptyView.findViewById(R.id.empty_view);
        emptyImageView = emptyView.findViewById(R.id.empty_view_bg);

        movieList.setEmptyView(emptyView);

        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                View loaderInd = findViewById(R.id.loading_indicator);
                loaderInd.setVisibility(View.VISIBLE);
                movieList.setClickable(false);
                pageBottom.setClickable(false);

                Movie currentMovie = movieAdapter.getItem(position);
                int movieId = currentMovie.getId();

                DETAILS_REQUEST_URL = DETAILS_REQUEST_PREFIX + String.valueOf(movieId);

                // Intent details = new Intent(MainActivity.this, MovieDetailsActivity.class);
                // details.putExtra(MOVIE_ID, movieId);

                new LoadTask().execute(DETAILS_REQUEST_URL);
                // details.putExtra(MOVIE_DETAILS, );

                // startActivity(details);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(MOVIE_LOADER_ID, null, this);

            int movieCount = QueryUtils.getMovieCount();
            int pages = (movieCount / movieLimit) + 1;

            totalPages.setText(String.valueOf(pages));
        } else {
            View loadIndicator = findViewById(R.id.loading_indicator);
            loadIndicator.setVisibility(View.GONE);
            pageBottom.setVisibility(View.GONE);

            currentPage.setText("?");
            totalPages.setText("?");

            emptyTextView.setText(R.string.no_internet);
            emptyImageView.setImageResource(R.drawable.no_wifi);
        }

        myDrawer = findViewById(R.id.nav_drawer);
        myDrawerToggle = new ActionBarDrawerToggle(this, myDrawer, R.string.open_str, R.string.close_str);
        myDrawerToggle.setDrawerIndicatorEnabled(true);

        myDrawer.addDrawerListener(myDrawerToggle);
        myDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        newRequest = new StringBuilder(YTS_BASE_URL);

                        currentPageNum = Integer.parseInt(YTS_DEFAULT_PAGE_NUMBER);

                        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                        ProgressBar loadInd = findViewById(R.id.loading_indicator);
                        loadInd.setVisibility(View.VISIBLE);

                        isQuality = false;
                        isRating = false;
                        isGenre = false;
                        isSort = false;
                        isOrder = false;
                        isLimit = false;
                        isSearch = false;

                        break;

                    case R.id.nav_filter:
                        AlertDialog.Builder filterBuilder = new AlertDialog.Builder(MainActivity.this);
                        filterBuilder.setTitle("Choose filter(s)");
                        final View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);
                        filterBuilder.setView(dialogView);

                        // Initialise Spinners
                        Spinner spinRating = dialogView.findViewById(R.id.spinner_rating);
                        Spinner spinQuality = dialogView.findViewById(R.id.spinner_quality);
                        Spinner spinGenre = dialogView.findViewById(R.id.spinner_genre);
                        Spinner spinSort = dialogView.findViewById(R.id.spinner_sort);
                        Spinner spinOrder = dialogView.findViewById(R.id.spinner_order);

                        /*
                         *  Initialise array adapters to attach to spinners for list of options.
                         */
                        ArrayAdapter ratingAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                                R.array.spinner_rating, android.R.layout.simple_spinner_dropdown_item);
                        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        ArrayAdapter genreAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                                R.array.spinner_genre, android.R.layout.simple_spinner_dropdown_item);
                        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        ArrayAdapter qualityAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                                R.array.spinner_quality, android.R.layout.simple_spinner_dropdown_item);
                        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        ArrayAdapter sortAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                                R.array.spinner_sort, android.R.layout.simple_spinner_dropdown_item);
                        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        ArrayAdapter orderAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                                R.array.spinner_order, android.R.layout.simple_spinner_dropdown_item);
                        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        /*
                         *  Set adapters to respective spinners and add OnItemSelectedListener
                         */
                        spinRating.setAdapter(ratingAdapter);
                        spinRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                ratingFilter = String.valueOf(i);
                                ratingItem = i;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        spinGenre.setAdapter(genreAdapter);
                        spinGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                genreFilter = (String) adapterView.getItemAtPosition(i);
                                genreItem = i;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        spinQuality.setAdapter(qualityAdapter);
                        spinQuality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                qualityFilter = (String) adapterView.getItemAtPosition(i);
                                qualityItem = i;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        spinSort.setAdapter(sortAdapter);
                        spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                sortFilter = ((String) adapterView.getItemAtPosition(i)).toLowerCase();
                                sortItem = i;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        spinOrder.setAdapter(orderAdapter);
                        spinOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                switch (i) {
                                    case 0:
                                        orderFilter = "asc";
                                        break;
                                    case 1:
                                        orderFilter = "desc";
                                        break;
                                    default:
                                        orderFilter = "desc";
                                }
                                orderItem = i;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });


                        final CheckBox cbQuality = dialogView.findViewById(R.id.cb_quality);
                        final CheckBox cbRating = dialogView.findViewById(R.id.cb_rating);
                        final CheckBox cbGenre = dialogView.findViewById(R.id.cb_genre);
                        final CheckBox cbSort = dialogView.findViewById(R.id.cb_sort);
                        final CheckBox cbOrder = dialogView.findViewById(R.id.cb_order);

                        /*
                         *  If statements to set previously added filters.
                         */

                        if (isLimit) {
                            EditText limitView = dialogView.findViewById(R.id.limit_edit_text);
                            limitView.setText(String.valueOf(limit));
                        }
                        if (isQuality) {
                            cbQuality.setChecked(true);
                            spinQuality.setSelection(qualityItem);
                        }
                        if (isRating) {
                            cbRating.setChecked(true);
                            spinRating.setSelection(ratingItem);
                        }
                        if (isGenre) {
                            cbGenre.setChecked(true);
                            spinGenre.setSelection(genreItem);
                        }
                        if (isSort) {
                            cbSort.setChecked(true);
                            spinSort.setSelection(sortItem);
                        }
                        if (isOrder) {
                            cbOrder.setChecked(true);
                            spinOrder.setSelection(orderItem);
                        }

                        filterBuilder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                newRequest = new StringBuilder(YTS_BASE_URL + YTS_QUERY_BEGINNER);

                                currentPageNum = Integer.parseInt(YTS_DEFAULT_PAGE_NUMBER);

                                EditText limitET = dialogView.findViewById(R.id.limit_edit_text);
                                String limitStr = limitET.getText().toString().trim();

                                if (!TextUtils.isEmpty(limitStr)) {
                                    limit = Integer.parseInt(limitStr);
                                }

                                if (TextUtils.isEmpty(limitStr) || (limit == 0)) {
                                    isLimit = false;
                                    newRequest.append(YTS_REQUEST_FILTER_LIMIT).append(YTS_DEFAULT_LIMIT);
                                } else if (limit > 50) {
                                    isLimit = true;
                                    limit = Integer.parseInt(YTS_MAX_LIMIT);
                                    newRequest.append(YTS_REQUEST_FILTER_LIMIT).append(YTS_MAX_LIMIT);
                                    Toast.makeText(MainActivity.this, "Limit exceeded. Set to maximum (50).", Toast.LENGTH_SHORT).show();
                                } else {
                                    isLimit = true;
                                    newRequest.append(YTS_REQUEST_FILTER_LIMIT).append(limitStr);
                                }

                                if (cbQuality.isChecked()) {
                                    isQuality = true;
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_QUALITY).append(qualityFilter);
                                } else {
                                    isQuality = false;
                                }
                                if (cbRating.isChecked()) {
                                    isRating = true;
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_RATING).append(ratingFilter);
                                } else {
                                    isRating = false;
                                }
                                if (cbGenre.isChecked()) {
                                    isGenre = true;
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_GENRE).append(genreFilter);
                                } else {
                                    isGenre = false;
                                }
                                if (cbSort.isChecked()) {
                                    isSort = true;
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_SORT).append(sortFilter);
                                } else {
                                    isSort = false;
                                }
                                if (cbOrder.isChecked()) {
                                    isOrder = true;
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_ORDER).append(orderFilter);
                                } else {
                                    isOrder = false;
                                }

                                if (isSearch) {
                                    newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_SEARCH).append(searchQueryEntered);
                                }

                                if (isQuality || isGenre || isRating || isSort || isOrder || isLimit) {
                                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                                    ProgressBar loadInd = findViewById(R.id.loading_indicator);
                                    loadInd.setVisibility(View.VISIBLE);

                                }
                            }
                        });

                        filterBuilder.setNegativeButton("REMOVE FILTER", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (cbQuality.isChecked() || cbGenre.isChecked() || cbRating.isChecked() ||
                                        cbSort.isChecked() || cbOrder.isChecked()) {
                                    newRequest = new StringBuilder(YTS_BASE_URL);

                                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                                    ProgressBar loadInd = findViewById(R.id.loading_indicator);
                                    loadInd.setVisibility(View.VISIBLE);

                                    currentPageNum = Integer.parseInt(YTS_DEFAULT_PAGE_NUMBER);
                                }

                                isQuality = false;
                                isRating = false;
                                isGenre = false;
                                isSort = false;
                                isOrder = false;
                                isLimit = false;
                                isSearch = false;

                                cbGenre.setChecked(false);
                                cbQuality.setChecked(false);
                                cbRating.setChecked(false);
                                cbSort.setChecked(false);
                                cbOrder.setChecked(false);
                                // Toast.makeText(MainActivity.this, "Filter Removed.", Toast.LENGTH_SHORT).show();
                            }
                        });

                        filterBuilder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        AlertDialog filterDialog = filterBuilder.create();
                        filterDialog.show();

                        break;

                    case R.id.nav_sort:
                        Snackbar.make(findViewById(android.R.id.content), "Sort disabled temporarily", Snackbar.LENGTH_SHORT).show();
                }

                myDrawer.closeDrawers();
                return true;
            }
        });

        findViewById(R.id.button_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage = findViewById(R.id.current_page_view);
                currentPageNum = Integer.parseInt(currentPage.getText().toString());

                if (currentPageNum > 1) {

                    currentPageNum--;

                    // currentPage.setText(String.valueOf(currentPageNum));

                    if (isQuality || isGenre || isRating || isSort || isOrder || isLimit) {
                        newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_PAGE_NUM).append(String.valueOf(currentPageNum));
                    } else {
                        newRequest.append(YTS_QUERY_BEGINNER).append(YTS_REQUEST_FILTER_PAGE_NUM).append(String.valueOf(currentPageNum));
                    }

                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                    ProgressBar loadInd = findViewById(R.id.loading_indicator);
                    loadInd.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage = findViewById(R.id.current_page_view);
                currentPageNum = Integer.parseInt(currentPage.getText().toString());

                int movieCount = QueryUtils.getMovieCount();
                int movieLimit;

                if (isLimit) {
                    movieLimit = limit;
                } else {
                    movieLimit = Integer.parseInt(YTS_DEFAULT_LIMIT);
                }

                int pages = (movieCount / movieLimit) + 1;

                if (currentPageNum < pages) {
                    currentPageNum++;

                    if (isQuality || isGenre || isRating || isSort || isOrder || isLimit) {
                        newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_PAGE_NUM).append(String.valueOf(currentPageNum));
                    } else {
                        newRequest = new StringBuilder(YTS_BASE_URL);
                        newRequest.append(YTS_QUERY_BEGINNER).append(YTS_REQUEST_FILTER_PAGE_NUM).append(String.valueOf(currentPageNum));
                    }

                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                    ProgressBar loadInd = findViewById(R.id.loading_indicator);
                    loadInd.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    TextView currentPage, totalPages;
    int currentPageNum = Integer.parseInt(YTS_DEFAULT_PAGE_NUMBER);

    @Override
    public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
        movieList.setClickable(false);
        return new MovieLoader(this, newRequest.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        movieList.setClickable(true);
        View loadIndicator = findViewById(R.id.loading_indicator);
        loadIndicator.setVisibility(View.GONE);

        emptyTextView.setText(R.string.no_movies);
        emptyImageView.setImageResource(R.drawable.empty_view_bg);

        movieAdapter.clear();

        if (movies != null && !movies.isEmpty()) {
            movieAdapter.addAll(movies);

            findViewById(R.id.page_bottom).setVisibility(View.VISIBLE);

            currentPage = findViewById(R.id.current_page_view);
            currentPage.setText(String.valueOf(currentPageNum));

            int movieCount = QueryUtils.getMovieCount();
            int movieLimit;

            if (isLimit) {
                movieLimit = limit;
            } else {
                movieLimit = Integer.parseInt(YTS_DEFAULT_LIMIT);
            }
            int pages = (movieCount / movieLimit) + 1;

            totalPages.setText(String.valueOf(pages));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder exitBuilder = new AlertDialog.Builder(this);
        exitBuilder.setMessage("Do you wish to exit?");
        exitBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        exitBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        exitBuilder.setNeutralButton("HOMEPAGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newRequest = new StringBuilder(YTS_BASE_URL);

                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                ProgressBar loadInd = findViewById(R.id.loading_indicator);
                loadInd.setVisibility(View.VISIBLE);
            }
        });

        AlertDialog exitDialog = exitBuilder.create();
        exitDialog.show();
    }

    private class LoadTask extends AsyncTask<String, Void, MovieDetails> {

        public LoadTask() {}

        @Override
        protected MovieDetails doInBackground(String... urls) {
            return QueryUtils.fetchMovieDetails(urls[0]);
        }

        @Override
        protected void onPostExecute(MovieDetails movieDetails) {
            Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
            intent.putExtra(MOVIE_DETAILS, movieDetails);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.movie_search:
                // Toast.makeText(this, "Search functionality temporarily disabled.", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder searchBuilder = new AlertDialog.Builder(MainActivity.this);
                searchBuilder.setTitle("Search for");
                final View searchView = getLayoutInflater().inflate(R.layout.search_dialog, null);
                searchBuilder.setView(searchView);

                final EditText searchQueryET = searchView.findViewById(R.id.search_query_et);

                searchBuilder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        searchQueryEntered = searchQueryET.getText().toString().trim();

                        if (!TextUtils.isEmpty(searchQueryEntered)) {
                            isSearch = true;
                            newRequest = new StringBuilder(YTS_BASE_URL + YTS_QUERY_BEGINNER);

                            if (searchQueryEntered.contains(" ")) {
                                searchQueryEntered = searchQueryEntered.replace(" ", "%");
                            }
                            newRequest.append(YTS_REQUEST_SEARCH).append(searchQueryEntered);

                            if (isLimit) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_LIMIT).append(String.valueOf(limit));
                            }

                            if (isQuality) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_QUALITY).append(qualityFilter);
                            }

                            if (isRating) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_RATING).append(ratingFilter);
                            }

                            if (isGenre) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_GENRE).append(genreFilter);
                            }

                            if (isSort) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_SORT).append(sortFilter);
                            }

                            if (isOrder) {
                                newRequest.append(YTS_QUERY_SEPARATOR).append(YTS_REQUEST_FILTER_ORDER).append(orderFilter);
                            }

                            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                            ProgressBar loadInd = findViewById(R.id.loading_indicator);
                            loadInd.setVisibility(View.VISIBLE);

                            currentPageNum = Integer.parseInt(YTS_DEFAULT_PAGE_NUMBER);
                            currentPage = findViewById(R.id.current_page_view);

                            currentPage.setText(YTS_DEFAULT_PAGE_NUMBER);

                            int movieCount = QueryUtils.getMovieCount();
                            int movieLimit;

                            if (isLimit) {
                                movieLimit = limit;
                            } else {
                                movieLimit = Integer.parseInt(YTS_DEFAULT_LIMIT);
                            }

                            int pages = (movieCount / movieLimit) + 1;

                        } else {
                            isSearch = false;
                        }
                    }
                });

                searchBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog searchDialog = searchBuilder.create();
                searchDialog.show();

                break;

        }
        return myDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
