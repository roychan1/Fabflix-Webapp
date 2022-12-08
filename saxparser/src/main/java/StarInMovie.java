public class StarInMovie {
    private String name;
    private String movieId;

    public void StarInMovie() {}
    public void StarInMovie(String name, String movieId) {
        this.name = name;
        this.movieId = movieId;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }
    public String getMovieId() {
        return movieId;
    }

    public String toString() {
        return "name: " + name +
                ", movieId: " + movieId;
    }
}
