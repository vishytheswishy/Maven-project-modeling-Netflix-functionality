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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "DashBoardServlet", urlPatterns = "/api/addMovie")
public class MovieInsertServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getSession().getAttribute("emp") == null) {
            httpResponse.sendRedirect("_dashboard.html");
        }
        String movieTitle = request.getParameter("movie_title");
        String movieYear = request.getParameter("movie_year");
        String movieDirector = request.getParameter("movie_director");
        String actorName = request.getParameter("actor_name");
        String actorDOB = request.getParameter("actor_dob");
        String movieGenre = request.getParameter("movie_genre");

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://18.222.116.172:3306/moviedb";

        if (actorDOB.equals("")) {
            actorDOB = "0";
        }

        System.out.println(actorDOB);
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();


            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);


            String checkQ = "select count(*) as num from movies as m where m.title='" + movieTitle +"' and m.year =" + movieYear +" and" +
                    " m.director = '" + movieDirector + "' ;";
            PreparedStatement checkquery = connection.prepareStatement(checkQ);
            ResultSet rs = checkquery.executeQuery(checkQ);
            String num = "2";
            while(rs.next()) {
                 num = rs.getString("num");
            }
            System.out.println(num);
            checkquery.close();
            rs.close();
            JsonObject responseJsonObject = new JsonObject();
            if (num.equals("0")) {
                String query = String.format("call add_movie('%s', %s, '%s', '%s' , %s, '%s');", movieTitle, movieYear, movieDirector, actorName, actorDOB, movieGenre);
                System.out.println(query);
                PreparedStatement insert = connection.prepareStatement(query);
//            System.out.println(insert.toString());
                insert.executeUpdate(query);
                responseJsonObject.addProperty("message", movieTitle + " with actor " + actorName + " and genre " + movieGenre +
                       " has been added to the database!");
                insert.close();
            } else {
                responseJsonObject.addProperty("message", movieTitle + " is already in the database!");
            }
            responseJsonObject.addProperty("status", "success");




            response.getWriter().write(responseJsonObject.toString());

            connection.close();


        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException | NamingException ex) {
            ex.printStackTrace();
        }
    }
}

