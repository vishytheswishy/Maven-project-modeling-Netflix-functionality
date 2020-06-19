/* AUTHOR: GIORGIO ATANASOV / VISHAAL YALAMANCHALI
BACKEND IMPLEMENTATION
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


// this annotation maps this Java Servlet Class to a URL
@WebServlet(name = "MovieServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

//            Statement statement = connection.createStatement();
            String query = "select m.title, m.year, m.director, m.id, substring_index(GROUP_CONCAT(distinct g.names SEPARATOR ', '),', ',3) as genres_list,\n" +
                    "substring_index(GROUP_CONCAT(DISTINCT '<a href=\"single-actor.html?id=', s.id, '\">', s.name , '</a>' ORDER BY s.name ASC SEPARATOR ', '), ', ', 3) as stars_list, t.rating \n" +
                    "from movies as m, top20ratings as t, stars_in_movies as sm, genres_in_movies as gm, genres as g, stars as s \n" +
                    "where m.id = t.movieID AND m.id = gm.movieID AND g.id = gm.genreID AND sm.movieID = m.id AND sm.starID = s.id \n" +
                    "GROUP BY m.title, m.year, m.director, t.rating, m.id ORDER BY t.rating DESC";
            PreparedStatement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            System.out.println(user.getUsername());
            while (rs.next()) {
                String title = rs.getString("title");
                String titleID = rs.getString("id");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String genre = rs.getString("genres_list");
                String star = rs.getString("stars_list");
                String rating = rs.getString("rating");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username",user.getUsername() );
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("titleID", titleID);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("star", star);
                jsonObject.addProperty("rating", rating);
                jsonArray.add(jsonObject);
            }
            out.write(jsonArray.toString());
            response.setStatus(200);
            rs.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }

        out.close();

    }


}
