package com.example.manik.ytsapi;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(@NonNull Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View movieItemView = convertView;

        if (movieItemView == null) {
            movieItemView = LayoutInflater.from(getContext()).inflate(R.layout.movie_item,
                    parent, false);
        }

        Movie currentMovie = getItem(position);

        ImageView movieIcon = movieItemView.findViewById(R.id.movie_icon);
        TextView movieTitle = movieItemView.findViewById(R.id.movie_title);
        TextView movieYear = movieItemView.findViewById(R.id.movie_year);
        TextView movieRating = movieItemView.findViewById(R.id.movie_rating);

        ProgressBar movieRatingRing = movieItemView.findViewById(R.id.movie_rating_bar);

        movieTitle.setText(currentMovie.getMovieTitle());

        String imgUrl = currentMovie.getImageUrl();

        // Bitmap imgBitmap = loadImage(currentMovie.getImageUrl());
        // movieIcon.setImageBitmap(imgBitmap);

        if (imgUrl == null || TextUtils.isEmpty(imgUrl)) {
            movieIcon.setImageResource(R.drawable.no_image_icon);
        } else {
            new ImageTask(movieIcon).execute(imgUrl);
        }

        movieYear.setText(currentMovie.getYear());
        movieRating.setText(currentMovie.getRating());

        double rating = Double.parseDouble(currentMovie.getRating());
        movieRatingRing.setProgress((int) (rating * 10));

        return movieItemView;
    }

    private class ImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        public ImageTask(ImageView imageView) {
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

    /*
    private Bitmap loadImage(String imageUrl) {

        Bitmap bm = null;

        URL url = null;

        try {
            url = new URL(imageUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);

            connection.connect();

            InputStream inputStream = connection.getInputStream();

            bm = BitmapFactory.decodeStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bm;
    }
    */

}
