import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.Map;


@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private DataSource dataSource;
    long startTime_TS;
    long startTime_TJ;
    long endTime_TS;
    long endTime_TJ;
    long elapsedTime_TS;
    long elapsedTime_TJ;

    private final String MYSQL_QUERY_HEAD =
            "SELECT title, year, director, movies.id, " +
            "   (SELECT rating FROM ratings WHERE ratings.movieId = movies.id) AS rating, " +
            "   GROUP_CONCAT(DISTINCT genres.name SEPARATOR ',') as genres, " +
            "   GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name ASC SEPARATOR ',') as stars, " +
            "   GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name ASC SEPARATOR ',') as star_ids " +
            "FROM " +
            "   movies " +
            "   JOIN stars_in_movies ON (movies.id = stars_in_movies.movieId) " +
            "   JOIN genres_in_movies ON (movies.id = genres_in_movies.movieId) " +
            "   JOIN stars ON (stars_in_movies.starId = stars.id) " +
            "   JOIN genres ON (genres_in_movies.genreId = genres.id) ";

    private final String MYSQL_QUERY_TAIL = "GROUP BY movies.id ";

    public void init(ServletConfig config) {
        System.out.println("INIT MOVIE SERVLET");
    }

    private String getQuery(Map<String, String[]> map) {
        String query = MYSQL_QUERY_HEAD;

        if (map.containsKey("startsWith")) {
            // browse by first character
            String character = map.get("startsWith")[0];
            if (character.equals("*")) {
                    query += "WHERE title REGEXP '^[^A-Za-z0-9].*$' " + MYSQL_QUERY_TAIL;
            } else {
                query += "WHERE title LIKE '" + character + "%' " + MYSQL_QUERY_TAIL;
            }
        } else if (map.containsKey("genre")) {
            // browse by genre
            query += MYSQL_QUERY_TAIL + "HAVING genres LIKE '%" + map.get("genre")[0] + "%' ";
        } else {
            // search
            String mySQLMovieConditions = "";
            if (map.containsKey("title")) {
//                mySQLMovieConditions += "WHERE title LIKE '%" + map.get("title")[0] + "%' ";
                mySQLMovieConditions += "WHERE title LIKE '%";
                for (String s : map.get("title")[0].trim().split("\\s+")) {
                    mySQLMovieConditions += s + "%";
                }
                mySQLMovieConditions += "' ";
            }
            if (map.containsKey("year")) {
                mySQLMovieConditions += mySQLMovieConditions.isEmpty() ?
                        "WHERE year = " + map.get("year")[0] + " " :
                        "AND year = " + map.get("year")[0] + " ";
            }
            if (map.containsKey("director")) {
                mySQLMovieConditions += mySQLMovieConditions.isEmpty() ?
                        "WHERE director LIKE '%" + map.get("director")[0] + "%' " :
                        "AND director LIKE '%" + map.get("director")[0] + "%' ";
            }
            String mySQLStarConditions = map.containsKey("star") ? "HAVING stars LIKE '%" +
                    map.get("star")[0] + "%' " : "";

            query += mySQLMovieConditions + MYSQL_QUERY_TAIL + mySQLStarConditions;
        }

        String formatQuery = "";
        String sort = map.get("sort")[0];
        if (sort.contains("ratingAsc")) {
            formatQuery += " ORDER BY rating ASC";
        } else if (sort.contains("ratingDes")) {
            formatQuery += " ORDER BY rating DESC";
        }
        if (sort.contains("titleAsc")) {
            if (formatQuery.equals("")) {
                formatQuery += " ORDER BY title ASC";
            } else {
                formatQuery += ", title ASC";
            }
        } else if (sort.contains("titleDes")) {
            if (formatQuery.equals("")) {
                formatQuery += " ORDER BY title DESC";
            } else {
                formatQuery += ", title DESC";
            }
        }

        String page = map.get("page")[0];
        String limit = map.get("limit")[0];
        formatQuery += " LIMIT " + limit + " OFFSET " + String.valueOf((Integer.valueOf(page) - 1) * Integer.parseInt(limit));

        query += formatQuery;
        return query;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Time an event in a program to nanosecond precision
        startTime_TS = System.nanoTime();
        startTime_TJ = System.nanoTime();

        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }


        File fileTS = new File(request.getServletContext().getRealPath("/ts_log.log"));
        File fileTJ = new File(request.getServletContext().getRealPath("/tj_log.log"));
        try {
            fileTS.createNewFile();
            fileTJ.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false",
//                    "aws",
//                    "MySQL123!");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(getQuery(request.getParameterMap()));
        ) {

            JsonArray jsonArray = new JsonArray();

            ResultSet rs = statement.executeQuery();
            endTime_TJ = System.nanoTime();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", rs.getString("movies.id"));
                jsonObject.addProperty("movie_title", rs.getString("title"));
                jsonObject.addProperty("movie_year", rs.getInt("year"));
                jsonObject.addProperty("movie_director", rs.getString("director"));
                jsonObject.addProperty("movie_genres", rs.getString("genres"));
                jsonObject.addProperty("movie_stars", rs.getString("stars"));
                jsonObject.addProperty("movie_starsId", rs.getString("star_ids"));
                jsonObject.addProperty("movie_rating", rs.getFloat("rating"));

                jsonArray.add(jsonObject);
            }

            rs.close();
            out.write(jsonArray.toString());
            response.setStatus(200);

            endTime_TS = System.nanoTime();
            elapsedTime_TS = endTime_TS - startTime_TS; // elapsed time in nano seconds. Note: print the values in nano seconds
            String strElapsedTime_TS = elapsedTime_TS + System.lineSeparator();
            System.out.println("Time elapsed for search servlet: " + strElapsedTime_TS);
            elapsedTime_TJ = endTime_TJ - startTime_TJ; // elapsed time in nano seconds. Note: print the values in nano seconds
            String strElapsedTime_TJ = elapsedTime_TJ + System.lineSeparator();
            System.out.println("Time elapsed for JDBC: " + strElapsedTime_TJ);

            try {
                Files.write(Path.of(request.getServletContext().getRealPath("/ts_log.log")), strElapsedTime_TS.getBytes(), StandardOpenOption.APPEND);
                Files.write(Path.of(request.getServletContext().getRealPath("/tj_log.log")), strElapsedTime_TJ.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                System.out.println(e);
            }
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