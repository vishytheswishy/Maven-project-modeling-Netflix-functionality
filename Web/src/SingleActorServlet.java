/* AUTHOR: GIORGIO ATANASOV / VISHAAL YALAMANCHALI
BACKEND IMPLEMENTATION
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
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


// API ENDPOINT DECLARATION
@WebServlet(name = "SingleActorServlet", urlPatterns = "/api/single-actor")
public class SingleActorServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;


	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String loginUser = "mytestuser";
		String loginPasswd = "mypassword";
		String serverName = "localhost";
		String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
		response.setContentType("application/json"); // Response mime type
		String id = request.getParameter("id");
		PrintWriter out = response.getWriter();
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource) envContext.lookup("jdbc/moviedbexample");
			Connection connection = ds.getConnection();

			String query = "SELECT * from stars as s, stars_in_movies as sm, movies as m " +
					"where s.id = ? and m.id = sm.movieId and sm.starId = s.id ORDER BY m.year DESC  ";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery();
			JsonArray jsonArray = new JsonArray();

			HttpSession session = request.getSession();
			String url = (String) session.getAttribute("rootSearch");

			int counter = 0;
			while (rs.next()) {
				String starId = rs.getString("starId");
				String starName = rs.getString("name");
				String starDob = rs.getString("birthYear");
				String movieId = rs.getString("movieId");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");

				JsonObject jsonObject = new JsonObject();

				if (counter == 0) {
					jsonObject.addProperty("redirect", url);
					counter++;
				}

				jsonObject.addProperty("star_id", starId);
				jsonObject.addProperty("star_name", starName);
				jsonObject.addProperty("star_dob", starDob);
				jsonObject.addProperty("movie_id", movieId);
				jsonObject.addProperty("movie_title", movieTitle);
				jsonObject.addProperty("movie_year", movieYear);
				jsonObject.addProperty("movie_director", movieDirector);


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
