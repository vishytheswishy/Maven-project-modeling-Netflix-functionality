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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;

// API ENDPOINT DECLARATION

@WebServlet(name = "SearchResultsServlet", urlPatterns = "/api/search_results")
public class SearchResultsServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    String loginUser = "mytestuser";
    String loginPasswd = "mypassword";


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTS = System.nanoTime();

        String contextPath = getServletContext().getRealPath("/");
        System.out.println(contextPath);
        String filePath = contextPath + "/log.txt";

        File file = new File(filePath);

        // if file doesn't exist create it
        if(!file.exists())
        {
            System.out.println("in here");
            file.createNewFile();
        }



        FileWriter writer = new FileWriter(file, true);
        file.createNewFile();




        response.setContentType("application/json");

        String movie_title = request.getParameter("title");
        String temp_title = movie_title;
        movie_title = "%" + movie_title + "%";

        String movie_year = request.getParameter("year");

        String movie_director = request.getParameter("director");
        movie_director = "%" + movie_director + "%";

        String actor_name = request.getParameter("actor");

        actor_name = "%" + actor_name + "%";

        String results = request.getParameter("results");

        String page = request.getParameter("pageNum");

        String titleOrder = request.getParameter("titleOrder");
        String rankOrder = request.getParameter("rankOrder");


        String url = "";
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();
        url = "search_results.html?" + queryString;
        System.out.println(url);


        request.getSession().setAttribute("rootSearch", url);


        int resultLimit = 0;
        if (results != null && !results.equals("")) {
            resultLimit = Integer.parseInt(results);
        } else
            resultLimit = 10;


        if (resultLimit > 100) {
            resultLimit = 100;
        }
        int pageNum = Integer.parseInt(page);
        int offCount = (pageNum - 1)* resultLimit;
        String device = request.getHeader("User-Agent");

        try {
            PrintWriter out = response.getWriter();
            String query = "select m.title, m.id , m.year, m.director, " +
                    "substring_index(GROUP_CONCAT(distinct '<a href=\"guidedSearch.html?movie=&genre=', g.names,'&pageNum=1&results=10', '\">', g.names, '</a>' SEPARATOR ', '),', ',3) as genres_list, " +
                    "substring_index(GROUP_CONCAT(distinct '<a href=\"single-actor.html?id=', s.id, '\">', s.name , '</a>' ORDER by numofmov DESC SEPARATOR ', '),', ', 3) as stars_list, r.rating, " +
                    "GROUP_CONCAT(distinct s.name ORDER BY numofmov DESC SEPARATOR ' , ') as stars_search from movies as m, " +
                    "ratings as r, starsnumofmovies as s, stars_in_movies as sm, genres as g, genres_in_movies as gm " +
                    "where sm.movieID = m.id AND s.id = sm.starID AND gm.genreID = g.id AND gm.movieID = m.id " +
                    "AND r.movieID = m.id ";

            if (device.contains("Android") || device.contains("Mobile")) {
                query = "select m.title, m.id , m.year, m.director, " +
                        "substring_index(GROUP_CONCAT(distinct g.names SEPARATOR ', '),', ',3) as genres_list, " +
                        "substring_index(GROUP_CONCAT(distinct s.name ORDER by numofmov DESC SEPARATOR ', '),', ', 3) as stars_list, r.rating, " +
                        "GROUP_CONCAT(distinct s.name ORDER BY numofmov DESC SEPARATOR ' , ') as stars_search from movies as m, " +
                        "ratings as r, starsnumofmovies as s, stars_in_movies as sm, genres as g, genres_in_movies as gm " +
                        "where sm.movieID = m.id AND s.id = sm.starID AND gm.genreID = g.id AND gm.movieID = m.id " +
                        "AND r.movieID = m.id ";
            }

            if (!movie_title.equals("%%")){
                query += String.format(" AND MATCH(m.title) AGAINST('%s*' IN BOOLEAN MODE)", temp_title);
            }
            if (!movie_director.equals("%%")) query += String.format(" AND m.director LIKE '%s'", movie_director);
            if (!movie_year.equals("")) query += String.format(" AND m.year = '%s'", movie_year);

            query += " GROUP BY m.title, m.id, m.year, m.director, r.rating";



            if (!actor_name.equals("%%")) query += String.format(" HAVING stars_search Like '%s'", actor_name);

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
            query += " LIMIT " + (resultLimit) + " offset " + (offCount) + ";";

            System.out.println(query);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // START QUERY TIME HERE
            long startTJ = System.nanoTime();
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            Statement statement = connection.createStatement();
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String title = rs.getString("title");
                String director = rs.getString("director");
                String genre = rs.getString("genres_list");
                String actors = rs.getString("stars_list");
                String rating = rs.getString("rating");
                String year = rs.getString("year");
                String movieID = rs.getString("id");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("actors", actors);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("movieID", movieID);
                jsonArray.add(jsonObject);
            }
            long endTJ = System.nanoTime();
            long TJ = endTJ - startTJ;
            long endTS = System.nanoTime();
            long TS = endTS - startTS;

            writer.write(TJ + " ");
            System.out.print("TJ value: " + TJ);
            writer.write(TS + "\n");
            System.out.print("TS value: " + TS);
            out.write(jsonArray.toString());
            rs.close();
            connection.close();
            statement.close();
            writer.close();


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
