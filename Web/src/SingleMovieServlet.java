/* AUTHOR: GIORGIO ATANASOV / VISHAAL YALAMANCHALI
BACKEND IMPLEMENTATION
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// API ENDPOINT DECLARATION

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

//    String loginUser = "mytestuser";
//    String loginPasswd = "mypassword";// user name and password
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = "localhost";
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";

        response.setContentType("application/json"); // Response mime type
        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            String query = "select m.title, m.year, m.director, s.id as starId, GROUP_CONCAT(DISTINCT g.names SEPARATOR ', ') as genres_list," +
                    "s.name as stars_list, t.rating from movies as m, movienullcheck as t, stars_in_movies as sm, genres_in_movies as gm," +
                    " genres as g, starsnumofmovies as s where m.id = ? AND gm.movieID = m.id AND sm.movieID = m.id " +
                    "AND t.ID = m.id AND sm.starID = s.id AND gm.genreID = g.id GROUP BY m.title, m.year, m.director, t.rating, s.id ORDER BY numofmov DESC";


            PreparedStatement statement = connection.prepareStatement(query);
            // Set the parameter represented by "?" in the query to the id we get from url,
            statement.setString(1, id);
            System.out.println(statement.toString());
            ResultSet rs = statement.executeQuery();

            HttpSession session = request.getSession();
            String url = (String) session.getAttribute("rootSearch");

            JsonArray jsonArray = new JsonArray();
            int counter = 0;

            while (rs.next()) {
                String title = rs.getString("title");
                String starID = rs.getString("starId");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String genre = rs.getString("genres_list");
                String star = rs.getString("stars_list");
                String rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();

                if (counter == 0) {
                    jsonObject.addProperty("redirect", url);
                    counter++;
                }

                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("star", star);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("starId", starID);
                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

}
