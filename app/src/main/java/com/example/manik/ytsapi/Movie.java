package com.example.manik.ytsapi;

import java.util.List;

public class Movie {

    private int mId;

    private String mTitle;

    private int mYear;

    private double mRating;

    private List<String> mGenres;

    private String mImageUrl;

    private String mSynopsis;

    private int mRuntime;

    public Movie(int id, String title, int year, double rating, List<String> genres, String imgUrl, String synopsis, int runtime) {
        mId = id;
        mTitle = title;
        mYear = year;
        mRating = rating;
        mImageUrl = imgUrl;
        mSynopsis = synopsis;
        mRuntime = runtime;
        mGenres = genres;
    }

    public int getId() {
        return mId;
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
}
