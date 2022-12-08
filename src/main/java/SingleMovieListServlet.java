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


@WebServlet(name = "SingleMovieListServlet", urlPatterns = "/api/single-movie-list")
public class SingleMovieListServlet extends HttpServlet {
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        PrintWriter out = response.getWriter();
        String query = "SELECT title, year, director, (SELECT rating FROM ratings WHERE movieId= ? ) AS rating FROM movies WHERE movies.id = ? ";
        String genreQuery = "SELECT name FROM genres_in_movies, genres WHERE genres_in_movies.movieId = ? AND genres_in_movies.genreId = genres.id ";
        String starQuery = "SELECT name, id FROM stars_in_movies, stars WHERE stars_in_movies.movieId = ? AND stars_in_movies.starId = stars.id ";

        // automatically closes connection with try-with-resources
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
             PreparedStatement starStatement = conn.prepareStatement(starQuery);
        ) {

            JsonObject jsonObject = new JsonObject();
            statement.setString(1, id);
            statement.setString(2, id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                jsonObject.addProperty("movie_title", rs.getString("title"));
                jsonObject.addProperty("movie_year", rs.getInt("year"));
                jsonObject.addProperty("movie_director", rs.getString("director"));
                jsonObject.addProperty("movie_rating", rs.getFloat("rating"));
            }

//            genres
            genreStatement.setString(1, id);
            ResultSet genreRs = genreStatement.executeQuery();
            JsonArray genreJsonArray = new JsonArray();
            while (genreRs.next()) {
                JsonObject genreJsonObject = new JsonObject();
                genreJsonObject.addProperty("genre", genreRs.getString("name"));
                genreJsonArray.add(genreJsonObject);
            }
            jsonObject.add("movie_genres", genreJsonArray);

//            stars
            starStatement.setString(1, id);
            ResultSet starRs = starStatement.executeQuery();
            JsonArray starJsonArray = new JsonArray();
            while (starRs.next()) {
                JsonObject starJsonObject = new JsonObject();
                starJsonObject.addProperty("star_name", starRs.getString("name"));
                starJsonObject.addProperty("star_id", starRs.getString("id"));
                starJsonArray.add(starJsonObject);
            }
            jsonObject.add("movie_stars", starJsonArray);

            rs.close();
            genreRs.close();
            starRs.close();

            out.write(jsonObject.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}