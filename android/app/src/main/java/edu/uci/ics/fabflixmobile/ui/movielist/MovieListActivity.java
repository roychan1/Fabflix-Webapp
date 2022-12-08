package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.uci.ics.fabflixmobile.Constants;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.*;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class MovieListActivity extends AppCompatActivity {

    private TextView noResultText;
    private TextView pageNumber;

    private final String baseURL = "http://" + Constants.host + ":" + Constants.port + "/" + Constants.domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noResultText = binding.noResultText;
        pageNumber = binding.pageNumber;
        final Button prevButton = binding.prevButton;
        final Button nextButton = binding.nextButton;
        prevButton.setOnClickListener(view -> onPrevClick());
        nextButton.setOnClickListener(view -> onNextClick());

        // TODO: this should be retrieved from the backend server
        getMoviesList(1);
    }

    private void getMoviesList(int page) {
        final ArrayList<Movie> movies = new ArrayList<>();

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest movieListRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movie-list?title=" +
                        getIntent().getStringExtra("query") +
                        "&page=" + page + "&limit=20&sort",
                response -> {
                    updateMovies(response, movies);
                    ListView listView = findViewById(R.id.list);
                    if (movies.isEmpty()) {
                        if (page == 1) {
                            noResultText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        noResultText.setVisibility(View.INVISIBLE);
                        pageNumber.setText("" + page);
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            SingleMoviePage.putExtra("title", movie.getName());
                            SingleMoviePage.putExtra("year", movie.getYear());
                            SingleMoviePage.putExtra("director", movie.getDirector());
                            SingleMoviePage.putExtra("stars", movie.getStars());
                            SingleMoviePage.putExtra("genres", movie.getGenres());
                            startActivity(SingleMoviePage);
                        });
                    }
                },
                error -> {
                    System.out.println(error);
                }
        );

        queue.add(movieListRequest);
    }

    private void updateMovies(String response, ArrayList<Movie> movies) {
        JsonArray responseArray = JsonParser.parseString(response).getAsJsonArray();
        responseArray.forEach((jsonElement) -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
//            String movieStars = jsonObject.get("movie_stars").getAsString();
//            String movieGenres = jsonObject.get("movie_genres").getAsString();


            movies.add(new Movie(jsonObject.get("movie_title").getAsString(),
                    (short)jsonObject.get("movie_year").getAsInt(),
                    jsonObject.get("movie_director").getAsString(),
                    jsonObject.get("movie_stars").getAsString(),
                    jsonObject.get("movie_genres").getAsString()));
//                    starsSubStringIndex < 0 ? movieStars : movieStars.substring(0, starsSubStringIndex),
//                    genresSubStringIndex < 0 ? movieGenres : movieGenres.substring(0, genresSubStringIndex)));
        });
    }


    public void onPrevClick() {
        try {
            int n = Integer.parseInt((String) pageNumber.getText());
            if (n > 1) {
                getMoviesList(--n);
            }
        } catch (NumberFormatException e) {
            System.out.println("onPrevClick: NumberFormatException " + (String) pageNumber.getText());
        }
    }

    public void onNextClick() {
        try {
            int n = Integer.parseInt((String) pageNumber.getText());
            getMoviesList(++n);
        } catch (NumberFormatException e) {
            System.out.println("onNextClick: NumberFormatException " + (String) pageNumber.getText());
        }
    }
}