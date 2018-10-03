package com.example.manik.ytsapi;

import java.io.Serializable;
import java.util.List;

public class MovieDetails implements Serializable{

    private String mImageUrl;

    private String mTitle;

    private int mYear;

    private double mRating;

    private List<String> mGenres;

    private String mSynopsis;

    private int mRuntime;

    private List<Cast> mCast;

    private String mYtTrailerCode;

    private String url_720p;

    private String url_1080p;

    private int mLikeCount;

    public MovieDetails(String url, String title, int year, double rating, List<String> genres,
                        String synopsis, int runtime, List<Cast> cast, String trailerCode,
                        String _720p, String _1080p, int likes) {

        mImageUrl = url;
        mTitle = title;
        mYear = year;
        mRating = rating;
        mGenres = genres;
        mSynopsis = synopsis;
        mRuntime = runtime;
        mCast = cast;
        mYtTrailerCode = trailerCode;
        url_720p = _720p;
        url_1080p = _1080p;
        mLikeCount = likes;

    }

    public String getMovieTitle() {
        return mTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getYear() {
        return String.valueOf(mYear);
    }

    public String getRuntime() {
        return String.valueOf(mRuntime);
    }

    public String getRating() {
        return String.valueOf(mRating);
    }

    public List<String> getGenres() {
        return mGenres;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public List<Cast> getCast() {
        return mCast;
    }

    public String getTrailerCode() {
        return mYtTrailerCode;
    }

    public String get720Url() {
        return url_720p;
    }

    public String get1080Url() {
        return url_1080p;
    }

    public String getLikes() {
        return String.valueOf(mLikeCount);
    }
}
