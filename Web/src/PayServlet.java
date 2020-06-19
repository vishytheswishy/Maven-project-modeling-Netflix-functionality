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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "PayServlet", urlPatterns = "/api/payment")
public class PayServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
        double cost = 0.0;
        for (int quantity: cart.values()) cost += quantity * 15.99;
        System.out.println("COST!!!!!!!: " + cost);
        if (cost != 0) {
            jsonObject.addProperty("cost", cost);
        } else {
            jsonObject.addProperty("cost", "The cart is currently empty!");
        }
        jsonArray.add(jsonObject);
        out.write(jsonArray.toString());
        out.close();
        response.setStatus(200);
    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCard = request.getParameter("creditCard");
        String expiration = request.getParameter("expirationDate");

        System.out.println("IN PAYMENT SERVLET");
        JsonObject responseJsonObject = new JsonObject();

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = "localhost";
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();


            Statement statement = connection.createStatement();
            String query = String.format("select c.expiration from creditcards as c where c.id = '%s'", creditCard);

            System.out.println(query);
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);

            HttpSession session = request.getSession();
            if (!rs.next()) { // if its false
                responseJsonObject.addProperty("message", "The credit card number that you have provided is invalid!");
            } else {
                String expirationDate = rs.getString("expiration");
                if (!expiration.equals("") && expiration.equals(expirationDate)) {
                    responseJsonObject.addProperty("firstName", firstName);
                    responseJsonObject.addProperty("lastName", lastName);
                    responseJsonObject.addProperty("creditCard", creditCard);
                    responseJsonObject.addProperty("expirationDate", expirationDate);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    System.out.println("THEY ARE NOT VALID");
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Your credit card details appear to be invalid please try again!");
                }
            }
            response.getWriter().write(responseJsonObject.toString());
            rs.close();
            connection.close();
            statement.close();

        } catch (SQLException | ClassNotFoundException ex) {
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
