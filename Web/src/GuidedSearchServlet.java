/* AUTHOR: GIORGIO ATANASOV / VISHAAL YALAMANCHALI
BACKEND IMPLEMENTATION
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;

// API ENDPOINT DECLARATION

@WebServlet(name = "GuidedResultsServlet", urlPatterns = "/api/guidedSearch")
public class GuidedSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        response.setContentType("application/json");
//        String serverName = request.getServerName();
//        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";

        String genre = request.getParameter("genre");
        String movie = request.getParameter("movie");
        genre = "%" + genre + "%";

        movie += "%";

        String results = request.getParameter("results");

        String page = request.getParameter("pageNum");

        String titleOrder = request.getParameter("titleOrder");

        String rankOrder = request.getParameter("rankOrder");


        String url = "";
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();
        url = "guidedSearch.html?" + queryString;
        System.out.println(url);


        request.getSession().setAttribute("rootSearch", url);

        int resultLimit = 0;
        if (results != null && !results.equals("")) {
            resultLimit = Integer.parseInt(results);
        } else {

            resultLimit = 10;

        }

        if (resultLimit > 100) {

            resultLimit = 100;

        }
        int pageNum = Integer.parseInt(page);
        int offCount = (pageNum - 1)* resultLimit;

        System.out.println(genre);
        System.out.println(movie);
        String alphaS = "0123456789abcdefghijklmnopqrstuvwxyz";
        char[] alpha = alphaS.toCharArray();
        try {
            PrintWriter out = response.getWriter();
            String query = "select m.title, m.id , m.year, m.director, GROUP_CONCAT(distinct g.names) as genre_search, " +
                    "substring_index(GROUP_CONCAT(distinct '<a href=\"guidedSearch.html?movie=&genre=', g.names,'&pageNum=1&results=10', '\">', g.names, '</a>' SEPARATOR ', '),', ',3) as genres_list, " +
                    "substring_index(GROUP_CONCAT(distinct '<a href=\"single-actor.html?id=', s.id, '\">', s.name , '</a>' ORDER by numofmov DESC SEPARATOR ', '),', ', 3) as stars_list, r.rating " +
                    " from movies as m, ratings as r, (select distinct s.name, s.id, count(DISTINCT sm.movieid) as numofmov from stars as s, stars_in_movies as sm where s.id = sm.starID group by s.name, s.id ORDER BY COUNT(sm.movieID) DESC) as s, stars_in_movies as sm, genres as g, genres_in_movies as gm " +
                    "where sm.movieID = m.id AND s.id = sm.starID AND gm.genreID = g.id AND gm.movieID = m.id " +
                    "AND s.id = sm.starID AND r.movieID = m.id ";

//
            if (movie.equals("*%")) {
                for( char i: alpha){
                    query += String.format(" AND m.title NOT LIKE '%s'", i + "%");
                }
            } else if (!movie.equals("%") && !movie.equals("null%"))
                query += String.format(" AND m.title LIKE '%s'", movie);

            query += " GROUP BY m.title, m.id, m.year, m.director, r.rating"; // ORDER BY r.rating DESC";


            if (!genre.equals("")) query += String.format(" HAVING genres_list like '%s'", genre);

            if (rankOrder.equals("asc")) {
                query += " ORDER BY r.rating " + rankOrder;
                if (titleOrder.equals("asc")) {
                    query += " , m.title " + titleOrder;
                }
                if (titleOrder.equals("desc")) {
                    query += " , m.title " + titleOrder;
                }
            } else if (rankOrder.equals("desc")) {
                query += " ORDER BY r.rating " + rankOrder;
                if (titleOrder.equals("asc")) {
                    query += " , m.title " + titleOrder;
                }
                if (titleOrder.equals("desc")) {
                    query += " , m.title " + titleOrder;
                }
            } else if (titleOrder.equals("asc")) {
                query += " ORDER BY m.title " + titleOrder;
            } else if (titleOrder.equals("desc")) {
                query += " ORDER BY m.title " + titleOrder;
            }
            if (offCount != 0) {
                offCount ++;
            }
            query += " limit " + (resultLimit) + " offset " + (offCount) + ";";

            System.out.println(query);
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();


            Statement statement = connection.createStatement();
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);
            System.out.println(query);
            JsonArray jsonArray = new JsonArray();
//            ResultSet tempRs = rs;
//            printResultSet(tempRs);
            while (rs.next()) {
                String title = rs.getString("title");
                String director = rs.getString("director");
                String genres = rs.getString("genres_list");
                String actors = rs.getString("stars_list");
                String rating = rs.getString("rating");
                String year = rs.getString("year");
                String movieID = rs.getString("id");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genre", genres);
                jsonObject.addProperty("actors", actors);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("movieID", movieID);
                jsonArray.add(jsonObject);
            }
            out.write(jsonArray.toString());
            rs.close();
            connection.close();
            statement.close();


        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}