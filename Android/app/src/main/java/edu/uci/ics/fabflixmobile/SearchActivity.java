package edu.uci.ics.fabflixmobile;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends ActionBarActivity {
    EditText movie;
    Button   search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchactivity);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        movie = findViewById(R.id.movie);
        search = findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch(view, movie.getText().toString());
            }
        });

    }

    public void performSearch(View view, String movie) {
        Intent searchResults = new Intent(this, ListViewActivity.class);
        searchResults.putExtra("title", movie);
        searchResults.putExtra("pageNum", 1);
        startActivity(searchResults);
    }
}

