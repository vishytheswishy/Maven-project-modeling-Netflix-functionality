import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;



@WebServlet(name = "GenreServlet", urlPatterns = "/api/genres")
public class GenreServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


//    protected void printResultSet(ResultSet rs) throws SQLException {
//        ResultSetMetaData rsmd = rs.getMetaData();
//        int columnsNumber = rsmd.getColumnCount();
//        while (rs.next()) {
//            for (int i = 1; i <= columnsNumber; i++) {
//                if (i > 1) System.out.print(",  ");
//                String columnValue = rs.getString(i);
//                System.out.print(columnValue + " " + rsmd.getColumnName(i));
//            }
//            System.out.println("");
//        }
//    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = request.getServerName();
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String query = "select g.names as names, g.id from genres as g";
        System.out.println(query);


        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
            Connection connection = ds.getConnection();

            Statement statement = connection.createStatement();
            PreparedStatement prep = connection.prepareStatement(query);
            ResultSet rs = prep.executeQuery(query);

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {

                String genre = rs.getString("names");
                String id = rs.getString("id");

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("id", id);
                jsonArray.add(jsonObject);

            }
            out.write(jsonArray.toString());





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
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("DO POST REQ");
    }
}
