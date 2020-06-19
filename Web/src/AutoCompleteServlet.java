
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

// server endpoint URL
@WebServlet("/auto_complete")
public class AutoCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /*
     * populate the Super hero hash map.
     * Key is hero ID. Value is hero name.
     */
    static {

    }

    public AutoCompleteServlet() {
        super();
    }

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String serverName = request.getServerName();
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
            response.setContentType("application/json");

            // setup the response json array
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String titleQ = request.getParameter("query");

            if (titleQ.length() > 3) {
                String[] searchList = titleQ.split(" ");

                String query = "select * from movies where MATCH (title) AGAINST (";
                int n = searchList.length;
                for (int i = 0; i < n; ++i) {
                    query += "? ";
                }
                query += String.format(" in boolean mode) OR ed(title, '%s') <= %d LIMIT 10", titleQ, 1);
                System.out.println(query);

                Class.forName("com.mysql.jdbc.Driver").newInstance();

                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
                Connection connection = ds.getConnection();

                PreparedStatement prep = connection.prepareStatement(query);
                for (int i = 1; i < n + 1; ++i) {
                    prep.setString(i, searchList[i - 1] + "* ");
                }
                ResultSet rs = prep.executeQuery();


                System.out.println(request.getContextPath());
                while (rs.next()) {
                    String movieID = rs.getString("id");
                    String movieTitle = rs.getString("title");
                    System.out.println(movieID);
                    System.out.println(movieTitle);

//                jsonArray.add(generateJsonObject(movieID, movieTitle, request.getContextPath() + "/single-movie.html?id=" + movieID));
                    jsonArray.add(generateJsonObject(movieID, movieTitle));

                }

                System.out.println(jsonArray.toString());

                response.getWriter().write(jsonArray.toString());

                System.out.println(prep);
                prep.close();
                connection.close();
                rs.close();
            }
            // return the empty json array if query is null or empty
        if (titleQ.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }
//
//            // search on superheroes and add the results to JSON Array
//            // this example only does a substring match
//            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
//
//            for (Integer id : superHeroMap.keySet()) {
//                String heroName = superHeroMap.get(id);
//                if (heroName.toLowerCase().contains(query.toLowerCase())) {
//                    jsonArray.add(generateJsonObject(id, heroName));
//                }
//            }
//
//            response.getWriter().write(jsonArray.toString());
//            return;
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String movieTitle) throws UnsupportedEncodingException {
//        System.out.println(url);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieID);
//        additionalDataJsonObject.addProperty("url", url);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
