package com.example.manik.ytsapi;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    private static int MOVIE_COUNT;

    private QueryUtils() {
    }

    public static int getMovieCount() {
        return MOVIE_COUNT;
    }

    private static URL createUrl(String requestUrl) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                inputStream = connection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("QUERY_UTILS", "Repsonse Code = " + connection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Movie> extractMovieList(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Movie> movies = new ArrayList<>();

        try {

            JSONObject movie = new JSONObject(jsonResponse);

            JSONObject movieData = movie.getJSONObject("data");

            MOVIE_COUNT = movieData.getInt("movie_count");

            if (MOVIE_COUNT != 0) {

                JSONArray movieArray = movieData.getJSONArray("movies");

                for (int i = 0; i < movieArray.length(); i++) {

                    JSONObject currentMovie = movieArray.getJSONObject(i);

                    int id = currentMovie.getInt("id");

                    String title = currentMovie.getString("title_english");

                    int year = currentMovie.getInt("year");

                    int runtime = currentMovie.getInt("runtime");

                    double rating = currentMovie.getDouble("rating");

                    String synopsis = currentMovie.getString("synopsis");

                    String imgUrl;

                    if (currentMovie.has("medium_cover_image")) {
                        imgUrl = currentMovie.getString("medium_cover_image");
                    } else {
                        imgUrl = null;
                    }

                    List<String> movieGenres;

                    if (currentMovie.has("genres")) {

                        JSONArray genres = currentMovie.getJSONArray("genres");

                        movieGenres = new ArrayList<>();

                        for (int j = 0; j < genres.length(); j++) {

                            String genre = genres.getString(j);

                            movieGenres.add(genre);

                        }

                    } else {

                        movieGenres = null;

                    }

                    movies.add(new Movie(id, title, year, rating, movieGenres, imgUrl, synopsis, runtime));

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movies;
    }

    public static List<Movie> fetchMovieList(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;

        try {

            jsonResponse = makeHttpRequest(url);

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Movie> movies = extractMovieList(jsonResponse);

        return movies;
    }

    private static MovieDetails extractMovieDetails(String detailsJsonResponse) {

        if (TextUtils.isEmpty(detailsJsonResponse)) {
            return null;
        }

        MovieDetails movieDetails = null;

        try {

            JSONObject base = new JSONObject(detailsJsonResponse);

            JSONObject detailsData = base.getJSONObject("data");

            JSONObject detailsMovie = detailsData.getJSONObject("movie");

            String dYtTrailerCode;

            if (detailsMovie.has("yt_trailer_code")) {

                dYtTrailerCode = detailsMovie.getString("yt_trailer_code");

            } else {

                dYtTrailerCode = null;

            }

            String dTitle = detailsMovie.getString("title_english");

            int dYear = detailsMovie.getInt("year");

            double dRating = detailsMovie.getDouble("rating");

            int dRuntime = detailsMovie.getInt("runtime");

            String dSynopsis = detailsMovie.getString("description_full");

            String dUrl;

            if (detailsMovie.has("medium_cover_image")) {
                dUrl = detailsMovie.getString("medium_cover_image");
            } else {
                dUrl = null;
            }

            List<String> dGenres;

            if (detailsMovie.has("genres")) {

                dGenres =  new ArrayList<>();

                JSONArray genreArr = detailsMovie.getJSONArray("genres");

                for (int j = 0; j < genreArr.length(); j++) {

                    String genre = genreArr.getString(j);

                    dGenres.add(genre);

                }

            } else {

                dGenres = null;

            }

            List<Cast> dCast;

            if (detailsMovie.has("cast")) {

                JSONArray castArr = detailsMovie.getJSONArray("cast");

                dCast = new ArrayList<>();

                for (int i = 0; i < castArr.length(); i++) {

                    JSONObject castObj = castArr.getJSONObject(i);

                    String cName = castObj.getString("name");

                    String cChar = castObj.getString("character_name");

                    dCast.add(new Cast(cName, cChar));

                }
            } else {
                dCast = null;
            }

            String _720url = null, _1080url = null;

            if (detailsMovie.has("torrents")) {

                JSONArray torrents = detailsMovie.getJSONArray("torrents");

                // TODO: get 720p and 1080p torrent urls.

                for (int urls = 0; urls < torrents.length(); urls++) {

                    JSONObject currentTorrent = torrents.getJSONObject(urls);
                    String quality = currentTorrent.getString("quality");

                    if (quality.equals("720p")) {
                        _720url = currentTorrent.getString("url");
                    } else if (quality.equals("1080p")) {
                        _1080url = currentTorrent.getString("url");
                    }
                }

            }

            int likes = 0;

            if (detailsMovie.has("like_count")) {

                likes = detailsMovie.getInt("like_count");

            }

            // TODO: return torrent urls in proper arguments below.
            movieDetails = new MovieDetails(dUrl, dTitle, dYear, dRating, dGenres, dSynopsis,
                    dRuntime, dCast, dYtTrailerCode, _720url, _1080url, likes);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieDetails;
    }

    public static MovieDetails fetchMovieDetails(String detailsRequestUrl) {

        URL url = createUrl(detailsRequestUrl);

        String detailsResponse = null;

        try {

            detailsResponse = makeHttpRequest(url);

        } catch (IOException e) {
            e.printStackTrace();
        }

        MovieDetails movieDetails = extractMovieDetails(detailsResponse);

        return movieDetails;

    }

}
