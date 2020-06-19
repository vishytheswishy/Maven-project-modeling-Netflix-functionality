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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        response.setContentType("application/json");

        String update = request.getParameter("update");
        String add = request.getParameter("add");
        String remove = request.getParameter("remove");
        String movieID = request.getParameter("movie");
        String quant = (request.getParameter("quantity"));
        System.out.println(quant);
        int quantity = 1;
        HttpSession session = request.getSession();


        if (session.getAttribute("cart") == null) {
            User user = new User("username");
            session.setAttribute("user", user);
            session.setAttribute("cart", user.getCart());
        }

//        User user = (User) session.getAttribute("user");

            PrintWriter out = response.getWriter();
            JsonArray jsonArray = new JsonArray();

            String status = "fill";
            HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
            if (cart.size() == 0) {
                System.out.println("CART IS EMPTY");
                status = "empty";
            } else {
                System.out.println("CART IS NOT EMPTY");
                for (String name : cart.keySet()) {
                    String key = name;
                    String value = cart.get(name).toString();
                    System.out.println(key + " " + value);
                }
            }

            if (add != null && add.equals("True")) {
                System.out.println("ADDING " + movieID + " TO THE CART");
                if (!quant.equals("null"))
                    quantity = Integer.parseInt(quant);
                 else
                     quantity = 1;

                 if(cart.containsKey(movieID)) {
                     cart.put(movieID, cart.get(movieID) + 1);
                 } else {
                     cart.put(movieID, quantity);
                 }

                session.setAttribute("cart", cart);
            }

            if(update != null && update.equals("True")) {
                if (quant.equals("null") || quant.equals("")) {
                    quant = "1";
                }
                System.out.println(quant);
                if (cart.containsKey(movieID) && !cart.get(movieID).equals(Integer.getInteger(quant))) {
                    System.out.println(movieID + " !!!!!! " + quant);
                    cart.put(movieID, Integer.parseInt(quant));
                }
                System.out.println("UPDATED THE CART!");

                for (String name : cart.keySet()) {
                    String key = name;
                    String value = cart.get(name).toString();
                    System.out.println(key + " " + value);
                }
            }



        if(remove != null && remove.equals("True")) {
            if (cart.containsKey(movieID)) {
                System.out.println(movieID + " !!!!!! " + quant);
                cart.remove(movieID);
            }
            System.out.println("REMOVED FROM CART!");

            for (String name : cart.keySet()) {
                String key = name;
                String value = cart.get(name).toString();
                System.out.println(key + " " + value);
            }
        }

        if (update.equals("") && remove.equals("") && add.equals("")) {
            cart.clear();
            session.setAttribute("cart", cart);
        }
            // USE CART TO BUILD QUERY
            StringBuilder query = new StringBuilder("select m.title, m.id, m.year from movies as m where ");

            try {

                User user = (User) session.getAttribute("user");
                Class.forName("com.mysql.jdbc.Driver").newInstance();

                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
                Connection connection = ds.getConnection();

                int counter = 0;
                for (HashMap.Entry<String, Integer> entry : cart.entrySet()) {
                    System.out.println(entry.getKey());
                    if (counter == 0) {
                        query.append("m.id = \"").append(entry.getKey()).append("\"");
                        counter++;
                    } else {
                        query.append(" OR m.id = \"").append(entry.getKey()).append("\"");
                    }
                } //for loop
                if (!cart.isEmpty()) {
                    System.out.println(query);
                    PreparedStatement statement = connection.prepareStatement(query.toString());
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        String title = rs.getString("title");
                        System.out.println(title);
                        String mID = rs.getString("id");
                        String year = rs.getString("year");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("username",user.getUsername() );
                        jsonObject.addProperty("movieID", mID);
                        jsonObject.addProperty("title", title);
                        jsonObject.addProperty("year", year);
                        jsonObject.addProperty("quantity", cart.get(mID));
                        jsonArray.add(jsonObject);
                    }
                    rs.close();
                    statement.close();
                    connection.close();
                }
                out.write(jsonArray.toString());
                out.close();
                response.setStatus(200);

            } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException | NamingException e) {
                e.printStackTrace();
            }
    }
}