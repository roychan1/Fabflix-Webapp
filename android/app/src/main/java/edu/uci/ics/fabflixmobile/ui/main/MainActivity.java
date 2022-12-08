package edu.uci.ics.fabflixmobile.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.*;

public class MainActivity extends AppCompatActivity {

    private EditText query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        query = binding.query;
        final Button button = binding.button;
        button.setOnClickListener(view -> search());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void search() {
//        finish();
        Intent MovieListPage = new Intent(MainActivity.this, MovieListActivity.class);
        MovieListPage.putExtra("query", query.getText().toString());
        startActivity(MovieListPage);
    }
}