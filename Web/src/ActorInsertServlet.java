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

@WebServlet(name = "ActorInsertServlet", urlPatterns = "/api/addActor")
public class ActorInsertServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    public static int counter = 0;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Counter: " + counter);
        counter += 1;
        String actor = request.getParameter("actor_name");
        String dob = request.getParameter("actor_dob");

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://18.222.116.172:3306/moviedb";

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (httpRequest.getSession().getAttribute("emp") == null) {
                httpResponse.sendRedirect("_dashboard.html");
            }


            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            String firstQuery = String.format("Select CONCAT('nm', substring( max(id) , 3 )  + 1) as newStar from stars");
            PreparedStatement prep = connection.prepareStatement(firstQuery);
            ResultSet rs = prep.executeQuery();


            String actorID = "";
            while (rs.next()) {
                actorID = rs.getString("newStar");
            }
            System.out.println(dob);
            String query = String.format("INSERT INTO stars (id, name, birthYear)\n" +
                    "VALUES ('%s', '%s', '%s');", actorID, actor, dob);
            System.out.println(query);
            PreparedStatement insert = connection.prepareStatement(query);
//            System.out.println(insert.toString());
            insert.executeUpdate(query);

            JsonObject responseJsonObject = new JsonObject();

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", actor + " : " + actorID + "has been added to the database!");



            response.getWriter().write(responseJsonObject.toString());
            connection.close();
            insert.close();
            rs.close();
            prep.close();

        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException | NamingException ex) {
            ex.printStackTrace();
        }
    }
}

