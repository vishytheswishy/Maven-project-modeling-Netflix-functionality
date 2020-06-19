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
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "EmployeeServlet", urlPatterns = "/api/_dashboard")
public class EmployeeLogin extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

//    @Resource(name = "jdbc/moviedb")
//    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

//        String username = "classta@email.edu";
//        String password = "classta";

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        JsonObject responseJsonObject = new JsonObject();

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            Statement statement = connection.createStatement();
            String query = String.format("select c.password from employees as c where c.email = '%s'", username);
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);


            HttpSession session = request.getSession();
            session.setAttribute("loggedIn", false);
            System.out.println(session.getAttribute("loggedIn"));

//            String url = "dashboard.html";
//            request.getSession().setAttribute("rootSearch", url);

            if (!rs.next()) { // if its false
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else {
                String sqlPassword = rs.getString("password");
                if (!sqlPassword.equals("") && sqlPassword.equals(password)) {
                    session.setAttribute("user", new User(username));
                    session.setAttribute("emp", new User(username));
                    session.setAttribute("loggedIn", true);
                    session.setAttribute("employeeLogin", true);

//                    session.setAttribute("cart", new HashMap<String, Float>());

                    System.out.println(session.getAttribute("loggedIn"));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            response.getWriter().write(responseJsonObject.toString());
            rs.close();
            connection.close();
            statement.close();

        }
        catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }


    }
}