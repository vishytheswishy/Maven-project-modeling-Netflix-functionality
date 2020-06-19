import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String device = request.getHeader("User-Agent");
        System.out.println(device);
        System.out.println(username);
        System.out.println(password);
        System.out.println(device);

//        String username = "a@email.com";
//        String password = "a2";

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
//        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        JsonObject responseJsonObject = new JsonObject();

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            Statement statement = connection.createStatement();

            String query = String.format("select c.password from customers as c where c.email = '%s'", username);
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);

            HttpSession session = request.getSession();
            session.setAttribute("loggedIn", false);
            System.out.println(session.getAttribute("loggedIn"));

            String url = "index.html";
            request.getSession().setAttribute("rootSearch", url);

            if (!rs.next()) { // if its false
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else {
                String sqlPassword = rs.getString("password");


                // Verify reCAPTCHA
                boolean reCaptcha = false;
                if (device.contains("Android") || device.contains("Mobile") || device.contains("Java/1.8.0_241")){
                    reCaptcha = true;
                    System.out.println("ON ANDROID/JMETER SETTING RECAPTCHA TRUE");
                } else {
                    try {
                        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
                        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
                        reCaptcha = RecaptchaVerify.verify(gRecaptchaResponse);

                    } catch (Exception ex) {
                    }
                }

//                sqlPassword.equals(password)
                if (!sqlPassword.equals("") && verifyPassword(username, password, "localhost") && reCaptcha) {
                    session.setAttribute("user", new User(username));
                    session.setAttribute("loggedIn", true);
                    session.setAttribute("cart", new HashMap<String, Float>());

                    System.out.println(session.getAttribute("loggedIn"));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }

                else if (!reCaptcha) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "invalid reCaptcha");
//                    responseJsonObject.addProperty("status", "fail");
//                    responseJsonObject.addProperty("message", "incorrect password");
                }
                else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            response.getWriter().write(responseJsonObject.toString());
            rs.close();
            connection.close();
            statement.close();

        }

        //        catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        catch (Exception ex) {
            ex.printStackTrace();
        }




    }
    private static boolean verifyPassword(String email, String password, String serverName) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        String query = String.format("SELECT * from customers where email='%s'", email);
        PreparedStatement prep = connection.prepareStatement(query);
        ResultSet rs = prep.executeQuery(query);

        boolean success = false;
        if (rs.next()) {
            // get the encrypted password from the database
            String encryptedPassword = rs.getString("password");

            // use the same encryptor to compare the user input password with encrypted password stored in DB
            success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
        }

        rs.close();
        statement.close();
        connection.close();

        System.out.println("verify " + email + " - " + password);

        return success;
    }
}
