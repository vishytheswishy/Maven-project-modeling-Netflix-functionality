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
@WebServlet(name = "MetaDataServlet", urlPatterns = "/api/metadata")
public class MetaDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet rs = metadata.getTables(null, null, "%", null);

            JsonArray jsonArray = new JsonArray();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("emp");
            while (rs.next()) {
                ResultSet columns = metadata.getColumns(null, null, rs.getString(3), null);
                String tableName = ( rs.getString(3) );
                System.out.println(tableName);
                String vars = "";
                while (columns.next()) {
                    vars += (columns.getString("COLUMN_NAME") + "-" + columns.getString("TYPE_NAME") + " , ");
                }
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", user.getUsername() );
                jsonObject.addProperty("tableNames", tableName);
                jsonObject.addProperty("variables", vars);
                jsonArray.add(jsonObject);
            }

            out.write(jsonArray.toString());
            response.setStatus(200);
            rs.close();
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
