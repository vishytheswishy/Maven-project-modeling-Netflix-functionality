import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
import java.io.PrintWriter;
import java.sql.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;import java.time.LocalDateTime;
import java.util.HashMap;

@WebServlet(name = "ConfirmServlet", urlPatterns = "/api/confirm")
public class ConfirmServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://18.222.116.172:3306/moviedb";
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            String query = "select * from customers as c where c.email = ?;";

            System.out.println(query);

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getUsername());
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            String ccld = "";
            while (rs.next()) {
                String ccID = rs.getString("id");
                ccld = ccID;
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("ccID", ccID);
                jsonObject.addProperty("firstName", firstName);
                jsonObject.addProperty("lastName", lastName);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            connection.close();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String time = LocalDate.now().format(formatter);
            System.out.println(time);

            HashMap<String, Integer> cart = (HashMap<String, Integer>) request.getSession().getAttribute("cart");
            String executeQ = "";

            for (HashMap.Entry<String, Integer> entry : cart.entrySet()) {
                    executeQ = "INSERT INTO sales(customerID , movieID, saleDate, quantity) VALUES (" + ccld + " , '"
                            + entry.getKey() + "' , '" + time + "' , '" + cart.get(entry.getKey()) + "' ); ";
                Connection insertConnect = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
                PreparedStatement insert = insertConnect.prepareStatement(executeQ);
                System.out.println(insert.toString());
                insert.executeUpdate(executeQ);
                insertConnect.close();
                insert.close();
            }
            System.out.println(executeQ);
            cart.clear();
            request.setAttribute("cart", cart);

            // add to sales table


            out.write(jsonArray.toString());
            response.setStatus(200);

            out.close();



        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);
        }
    }
}
