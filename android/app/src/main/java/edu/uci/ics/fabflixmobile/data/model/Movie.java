package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;
    private final short year;
    private final String director;
    private final String stars;
    private final String genres;

    public Movie(String name, short year, String director, String stars, String genres) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
    }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getStars() {
        return stars;
    }

    public String getGenres() {
        return genres;
    }

}