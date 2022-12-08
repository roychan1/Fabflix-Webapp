import java.util.ArrayList;

public class Movie {
    private String id;
    private String title;
    private Integer year;
    private String director;
    private ArrayList<String> genres;

    public void Movie() {}
    public void Movie(String id, String title, Integer year, String director, ArrayList<String> genres) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
    public Integer getYear() {
        return year;
    }

    public void setDirector(String director) {
        this.director = director;
    }
    public String getDirector() {
        return director;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }
    public void addGenre(String genre) {
        this.genres.add(genre);
    }
    public ArrayList<String> getGenres() {
        return genres;
    }

    public String toString() {
        String genreString = genres == null ? "null" : genres.toString();
        return "id: " + id +
                ", title: " + title +
                ", year: " + year +
                ", director: " + director +
                ", genres: " + genreString;
    }
}
