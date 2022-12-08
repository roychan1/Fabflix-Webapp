package edu.uci.ics.fabflixmobile.ui.singlemovie;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;

public class SingleMovieActivity extends AppCompatActivity {

    private TextView title;
    private TextView year;
    private TextView director;
    private TextView stars;
    private TextView genres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        title = binding.title;
        year = binding.year;
        director = binding.director;
        stars = binding.stars;
        genres = binding.genres;

        title.setText(getIntent().getStringExtra("title"));
        year.setText("" + getIntent().getShortExtra("year", (short)0));
        director.setText("Director:  " + getIntent().getStringExtra("director"));
        stars.setText("Stars:  " + getIntent().getStringExtra("stars"));
        genres.setText("Genres:  " + getIntent().getStringExtra("genres"));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
