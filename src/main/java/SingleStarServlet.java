import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;


@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        // Retrieve parameter id from url request
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        PrintWriter out = response.getWriter();

        String starQuery = "SELECT name, birthYear FROM stars WHERE id= ? ";
        String movieQuery = "SELECT movieId, title FROM stars_in_movies, movies WHERE starId= ? AND movies.id=movieId ";

        // automatically closes connection with try-with-resources
        try (Connection conn = dataSource.getConnection();
            PreparedStatement starStatement = conn.prepareStatement(starQuery);
            PreparedStatement movieStatement = conn.prepareStatement(movieQuery);
        ) {

            JsonObject jsonObject = new JsonObject();
            starStatement.setString(1, id);
            ResultSet starRs = starStatement.executeQuery();

            while (starRs.next()) {
                jsonObject.addProperty("star_name", starRs.getString("name"));
                if (starRs.getInt("birthYear") == 0) {
                    // getInt returns 0 when SQL item is null
                    jsonObject.addProperty("star_birth_year", "NOT AVAILABLE");
                } else {
                    jsonObject.addProperty("star_birth_year", starRs.getInt("birthYear"));
                }
            }

            movieStatement.setString(1, id);
            ResultSet movieRs = movieStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (movieRs.next()) {
                JsonObject movieJsonObject = new JsonObject();

                movieJsonObject.addProperty("movie_id", movieRs.getString("movieId"));
                movieJsonObject.addProperty("movie_title", movieRs.getString("title"));

                jsonArray.add(movieJsonObject);
            }

            jsonObject.add("star_movies_table", jsonArray);

            starRs.close();
            movieRs.close();

            out.write(jsonObject.toString());
            response.setStatus(200);
        } catch(Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            e.printStackTrace();

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}