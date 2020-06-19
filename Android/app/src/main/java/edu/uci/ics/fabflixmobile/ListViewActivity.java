package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListViewActivity extends Activity {
    private final String resultLength = "20";
    private String url = "https://18.223.2.54:8443/122bproject1/api/search_results?&results=20&title=";
    private ArrayList<Movie> movies = new ArrayList<>();
    JSONArray results = null;
    JSONObject obj = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        //this should be retrieved from the database and the backend server
        Bundle bundle = getIntent().getExtras();
        String movieTitle = bundle.getString("title");
        int pageNum = bundle.getInt("pageNum");
        Log.d("PAGENUMBER: ", Integer.toString(pageNum));

        TextView pageLabel = findViewById(R.id.pageNum);
        pageLabel.setText("Page Number: " +  Integer.toString(pageNum));
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url + movieTitle + "&pageNum=" + pageNum + "&titleOrder=&rankOrder=&year=&director=&actor=",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("response:", response);
                            try {
                                Log.d("URL ", url + movieTitle + "&pageNum=" + pageNum + "&titleOrder=&rankOrder=&year=&director=&actor=");
                                JSONArray jsonObject = new JSONArray(response);
                                parseResults(jsonObject);
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject jo = jsonArray.getJSONObject(i);
//
//                                    // Do you fancy stuff
//                                    // Example: String gifUrl = jo.getString("url");
//                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Display the first 500 characters of the response string.

                        }
                    }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error:", error.toString());
                    }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);


    }
    private void parseResults(JSONArray response) throws JSONException {
        movies.clear();
        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);
        for(int i = 0; i < response.length(); ++i) {
            JSONObject objJson = response.getJSONObject(i);
            movies.add(new Movie(objJson.getString("movieID"),objJson.getString("title"), (short) objJson.getInt("year")));
        }
        ListView listView = findViewById(R.id.list);
        Button back = findViewById(R.id.back);
        Button next = findViewById(R.id.next);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Intent searchResults = new Intent(ListViewActivity.this, SingleMovieActivity.class);
                searchResults.putExtra("id", movie.getID());
                startActivity(searchResults);
            }
        });



        //  BACK BUTTON!!!!!
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = getIntent().getExtras();
                String movie = bundle.getString("title");
                int pageNum = bundle.getInt("pageNum");
                if (pageNum > 1) {
                    Intent intents = new Intent(ListViewActivity.this, ListViewActivity.class);
                    Log.d("INSIDE HERE", "INSIDE");
                    intents.putExtra("pageNum", pageNum - 1);
                    intents.putExtra("title", movie);
                    startActivity(intents);
                }
            }
        });



        //  NEXT BUTTON!!!!!
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = getIntent().getExtras();
                String movie = bundle.getString("title");
                int pageNum = bundle.getInt("pageNum");
                if (pageNum >= 0) {
                    Intent intents = new Intent(ListViewActivity.this, ListViewActivity.class);
                    Log.d("INSIDE HERE", "INSIDE");
                    intents.putExtra("pageNum", pageNum + 1);
                    intents.putExtra("title", movie);
                    startActivity(intents);
                }
            }
        });



    }
}