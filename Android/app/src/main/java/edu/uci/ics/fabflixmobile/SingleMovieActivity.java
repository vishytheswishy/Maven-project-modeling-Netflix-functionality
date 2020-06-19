package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SingleMovieActivity extends Activity {

    private TextView movies,years,genres,stars, director, rating;
    private String url = "https://18.223.2.54:8443/122bproject1/api/single-movie?id=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemoviepage);

        movies = findViewById(R.id.movie);
        Log.d("proof", movies.toString());
        director = findViewById(R.id.director);
        years = findViewById(R.id.year);
        rating = findViewById(R.id.rating);
        genres = findViewById(R.id.genres);
        stars = findViewById(R.id.stars);

        //this should be retrieved from the database and the backend server
        Bundle bundle = getIntent().getExtras();
        String movieTitle = bundle.getString("id");

        Log.d("searchparameter", "Searching for " + movieTitle);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url + movieTitle,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            movies.setText("penis");
                            JSONArray jsonObject = new JSONArray(response);
                            parseResults(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display the first 500 characters of the response string.

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("cancer", error.toString());
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);


    }
    private void parseResults(JSONArray response) throws JSONException {
        Log.d("aids", response.toString());
        JSONObject objJsonDetails = response.getJSONObject(0);
        movies.setText(objJsonDetails.getString("title"));
        director.setText("Director: " +objJsonDetails.getString("director"));
        years.setText("Year: " + objJsonDetails.getString("year"));
        genres.setText("Genre: " + objJsonDetails.getString("genre"));
        String title = objJsonDetails.getString("rating");
        if (!title.equals("0")) {
            rating.setText("Rating: " + objJsonDetails.getString("rating"));
        }
        String starsL = "";
        for(int i = 0; i < response.length(); ++i) {
            JSONObject objJson = response.getJSONObject(i);
            starsL += (objJson.getString("star") + ", ");
        }
        stars.setText("Actors: " + starsL);
    }
}