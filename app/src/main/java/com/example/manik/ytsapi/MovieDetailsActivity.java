package com.example.manik.ytsapi;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String MOVIE_DETAILS = "com.example.manik.ytsapi.movie_details";

    private static final String YT_APP_PREFIX = "vnd.youtube:";
    private static final String YT_WEB_PREFIX = "https://www.youtube.com/watch?v=";

    private String mTitle, mYear, mRating, mSynopsis, mUrl, mTrailer, mRuntime, mLikes;
    private String m720pUrl, m1080pUrl;
    private int mProgress;

    private List<String> genresList;
    private List<Cast> castList;

    private TextView movieTitle, movieYear, movieGenres, movieRating, movieSynopsis, movieRuntime, movieLikes;
    private TextView _720p, _1080p;
    private ProgressBar movieRatingBar;
    private ImageView movieIcon;

    private ImageView movieTrailer;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout castLayout = findViewById(R.id.list_layout);

        _720p = findViewById(R.id.torrent_720p);
        _1080p = findViewById(R.id.torrent_1080p);

        movieIcon = findViewById(R.id.movie_details_icon);
        movieTitle = findViewById(R.id.movie_details_title);
        movieYear = findViewById(R.id.movie_details_year);
        movieGenres = findViewById(R.id.movie_details_genres);
        movieRating = findViewById(R.id.movie_details_rating);
        movieRatingBar = findViewById(R.id.movie_details_rating_bar);
        movieSynopsis = findViewById(R.id.details_synopsis_view);
        movieTrailer = findViewById(R.id.play_trailer);
        movieRuntime = findViewById(R.id.movie_runtime);
        movieLikes = findViewById(R.id.like_count);

        Intent receivedIntent = getIntent();

        MovieDetails mDetails = (MovieDetails) receivedIntent.getSerializableExtra(MOVIE_DETAILS);
        mTitle = mDetails.getMovieTitle();
        mYear = mDetails.getYear();
        mRating = mDetails.getRating();
        mProgress = (int) (Double.parseDouble(mDetails.getRating()) * 10);
        mSynopsis = mDetails.getSynopsis();
        mUrl = mDetails.getImageUrl();
        mTrailer = mDetails.getTrailerCode();
        m720pUrl = mDetails.get720Url();
        m1080pUrl = mDetails.get1080Url();
        mLikes = mDetails.getLikes();
        mRuntime = mDetails.getRuntime();

        if (mTrailer == null || TextUtils.isEmpty(mTrailer)) {
            movieTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MovieDetailsActivity.this, "No trailer available.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            movieTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YT_APP_PREFIX + mTrailer));
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YT_WEB_PREFIX + mTrailer));

                    if (appIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(appIntent);
                    } else {
                        getApplicationContext().startActivity(webIntent);
                    }
                }
            });
        }

        StringBuilder genresStr = new StringBuilder();
        genresList = mDetails.getGenres();

        if (genresList != null) {

            for (String s : genresList) {
                genresStr.append(s).append("  ");
            }

        } else {

            genresStr.append("---");

        }
        // This line is used to find number of records to be added.
        castList = mDetails.getCast();

        if (castList != null) {

            int castNum = castList.size();

            for (int i = 0; i < castNum; i++) {

                View view = getLayoutInflater().inflate(R.layout.movie_cast_item, null, false);

                TextView castName = view.findViewById(R.id.cast_name);
                TextView castChar = view.findViewById(R.id.cast_character);

                castName.setText(castList.get(i).getCastName());
                castChar.setText(castList.get(i).getCastChar());

                castLayout.addView(view);

            }
        } else {

            TextView noCast = new TextView(this);
            noCast.setText(getString(R.string.no_cast_info));
            noCast.setPadding(20,20,20,20);
            noCast.setTextColor(Color.WHITE);
            noCast.setTypeface(Typeface.MONOSPACE);

            castLayout.addView(noCast);

        }

        _720p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m720pUrl == null || TextUtils.isEmpty(m720pUrl)) {
                    Toast.makeText(MovieDetailsActivity.this, "No link for 720p download.", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder downloader = new AlertDialog.Builder(MovieDetailsActivity.this);
                    downloader.setMessage("Download 720p torrent file?");
                    downloader.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent dnld720p = new Intent(Intent.ACTION_VIEW, Uri.parse(m720pUrl));
                            startActivity(dnld720p);
                        }
                    });
                    downloader.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = downloader.create();
                    dialog.show();

                }
            }
        });

        _1080p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m1080pUrl == null || TextUtils.isEmpty(m1080pUrl)) {
                    Toast.makeText(MovieDetailsActivity.this, "No link for 1080p download.", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder downloader = new AlertDialog.Builder(MovieDetailsActivity.this);
                    downloader.setMessage("Download 1080p torrent file?");
                    downloader.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent dnld1080p = new Intent(Intent.ACTION_VIEW, Uri.parse(m1080pUrl));
                            startActivity(dnld1080p);
                        }
                    });
                    downloader.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = downloader.create();
                    dialog.show();
                }
            }
        });


        /*
        if (castList != null && !castList.isEmpty()) {
            castAdapter.addAll(castList);
        }
        */

        movieTitle.setText(mTitle);
        movieYear.setText(mYear);
        movieRating.setText(mRating);
        movieRatingBar.setProgress(mProgress);
        movieSynopsis.setText(mSynopsis);
        movieGenres.setText(genresStr.toString());
        movieLikes.setText(mLikes);
        movieRuntime.setText(mRuntime + " mins");

        // castListView.setAdapter(castAdapter);

        if (mUrl != null && !TextUtils.isEmpty(mUrl)) {
            new ImageTask(movieIcon).execute(mUrl);
        } else {
            movieIcon.setImageResource(R.drawable.no_image_icon);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class ImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        ImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bm = null;

            try {
                URL imgUrl = new URL(url);
                InputStream inputStream = imgUrl.openStream();
                bm = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    boolean isLiked = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.like_dislike:
                if (!isLiked) {
                    item.setIcon(R.drawable.ic_likes);
                    Toast.makeText(this, "Added to Liked Movies", Toast.LENGTH_SHORT).show();
                    isLiked = true;
                } else {
                    item.setIcon(R.drawable.ic_likes_def);
                    Toast.makeText(this, "Removed from Liked Movies", Toast.LENGTH_SHORT).show();
                    isLiked = false;
                }

        }
        return super.onOptionsItemSelected(item);
    }
}
