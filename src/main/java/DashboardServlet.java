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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.sql.PreparedStatement;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private DataSource dataSource;
    private DataSource writeDataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
//            writeDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-write");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse) (HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", "null connection to db");
                out.write(jsonObject.toString());

                response.setStatus(500);
            }

            JsonArray jsonArray = new JsonArray();
            PreparedStatement statement = conn.prepareStatement("Show tables");
            ResultSet rs = statement.executeQuery("Show tables");

            // metadata
            while (rs.next()) {
                String table = rs.getString(1);
                String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table + "'";
                PreparedStatement tableStatement = conn.prepareStatement(query);
                ResultSet tableRs = tableStatement.executeQuery(query);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("table", table);
                String colType = "";
                while (tableRs.next()) {
                    colType += tableRs.getString("COLUMN_NAME") + "(" +
                            tableRs.getString("DATA_TYPE") + "), ";
                }
                jsonObject.addProperty("colType", colType.substring(0,colType.length()-2));
                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();
            out.write(jsonArray.toString());
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

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            writeDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-write");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = writeDataSource.getConnection()) {
            if (conn == null) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", "null connection to db");
                out.write(jsonObject.toString());

                response.setStatus(500);
            }

            JsonObject jsonObject = new JsonObject();

            // Add a new star
            if (request.getParameter("add").equals("star")) {
                String name = request.getParameter("name");
                String birthYear = request.getParameter("birth-year");

                System.out.println("Adding new star");
                String query = "SELECT MAX(id) as id FROM stars;";
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery(query);
                String new_id = "";
                while (rs.next()) {
                    String current_id = rs.getString("id");
                    int current_id_int = Integer.parseInt(current_id.replace("nm", ""));
                    new_id = "nm" + (current_id_int + 1);
                }
                if (birthYear.equals("")) {
                    String queryInsert = "INSERT INTO stars VALUES ('" + new_id + "','" + name + "',null);";
                    PreparedStatement updateStatement = conn.prepareStatement(queryInsert);
                    updateStatement.executeUpdate(queryInsert);
                } else {
                    String queryInsert = "INSERT INTO stars VALUES ('" + new_id + "','" + name + "'," + birthYear + ");";
                    PreparedStatement updateStatement = conn.prepareStatement(queryInsert);
                    updateStatement.executeUpdate(queryInsert);
                }
                System.out.print(new_id);
                jsonObject.addProperty("star_id", new_id);
                rs.close();
                statement.close();

            } else {     // Add a new movie
                String movie_id = "";
                Integer genre_id = null;
                String star_id = null;
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String price = request.getParameter("price");
                String star = request.getParameter("star");
                String birthYear = request.getParameter("birth-year");
                String genre = request.getParameter("genre");

                // check for duplicate movie
                String query_movie_exists = "SELECT * FROM movies WHERE title='" + title + "';";
                PreparedStatement movieExistsStatement = conn.prepareStatement(query_movie_exists);
                ResultSet movieExistsRs = movieExistsStatement.executeQuery(query_movie_exists);
                jsonObject.addProperty("duplicate", true);
                while (!movieExistsRs.next()) {
                    System.out.println("Adding New Movie");
                    jsonObject.addProperty("duplicate", false);
                    String query_movie_id = "SELECT id AS movie_id FROM movies WHERE id REGEXP '^tt[0-9]{7}$' ORDER BY id DESC limit 1";
                    PreparedStatement movieStatement = conn.prepareStatement(query_movie_id);
                    ResultSet movieRs = movieStatement.executeQuery(query_movie_id);
                    while (movieRs.next()) {
                        String current_id = movieRs.getString("movie_id");
                        System.out.println(current_id);
                        int current_id_int = Integer.parseInt(current_id.replace("tt", ""));
                        System.out.println(current_id_int);
                        movie_id = "tt" + ((((double)++current_id_int / 10000000.0) + "").substring(2));
                        System.out.println("Movie ID: " + movie_id);
                    }
                    jsonObject.addProperty("movie_id", movie_id);
                    movieRs.close();
                    movieStatement.close();

                    if (!genre.equals("")) {
                        String query_genre_exists = "SELECT * FROM genres WHERE name = '" + genre + "';";
                        PreparedStatement genreExistsStatement = conn.prepareStatement(query_genre_exists);
                        ResultSet genreExistsRs = genreExistsStatement.executeQuery(query_genre_exists);
                        while (genreExistsRs.next()) {
                            genre_id = genreExistsRs.getInt("id");
                            System.out.println("Existing genre: " + genre_id);
                        }
                        if (genre_id == null) {
                            String query_genre_id = "SELECT MAX(id) as genre_id FROM genres;";
                            PreparedStatement genreStatement = conn.prepareStatement(query_genre_id);
                            ResultSet genreRs = genreStatement.executeQuery(query_genre_id);
                            while (genreRs.next()) {
                                genre_id = genreRs.getInt("genre_id") + 1;
                            }
                            genreRs.close();
                            genreStatement.close();
                            System.out.println("New genre: " + genre_id);
                        }
                        genreExistsRs.close();
                        genreExistsStatement.close();
                    }
                    jsonObject.addProperty("genre_id", genre_id);


                    if (!star.equals("")) {
                        String query_star_exists = "SELECT * FROM stars WHERE name = '" + star + "';";
                        PreparedStatement starExistsStatement = conn.prepareStatement(query_star_exists);
                        ResultSet starExistsRs = starExistsStatement.executeQuery(query_star_exists);
                        while (starExistsRs.next()) {
                            star_id = starExistsRs.getString("id");
                            birthYear = starExistsRs.getString("birthYear");
                            System.out.println("Existing star: " + star_id);
                        }
                        if (star_id == null) {
                            String query_star_id = "SELECT MAX(id) as star_id FROM stars;";
                            PreparedStatement starStatement = conn.prepareStatement(query_star_id);
                            ResultSet starRs = starStatement.executeQuery(query_star_id);
                            while (starRs.next()) {
                                String current_id = starRs.getString("star_id");
                                System.out.println(current_id);
                                int current_id_int = Integer.parseInt(current_id.replace("nm", ""));
                                System.out.println(current_id_int);
                                star_id = "nm" + (current_id_int + 1);
                                System.out.println("New star: " + star_id);
                            }
                            starRs.close();
                            starStatement.close();
                        }
                        starExistsRs.close();
                        starExistsStatement.close();
                    }
                    jsonObject.addProperty("star_id", star_id);
                    String query = "";
                    if (star.equals("") || (!star.equals("") && birthYear.equals(""))) {
                        System.out.println("Creating query with stored procedure...");
                        query = "SELECT add_movie('" + movie_id + "'," + genre_id + ",'" + star_id + "','" + title
                                + "'," + year + ",'" + director + "','" + price + "','" + star
                                + "', null, '" + genre + "');";
                    } else {
                        query = "SELECT add_movie('" + movie_id + "'," + genre_id + ",'" + star_id + "','" + title
                                + "'," + year + ",'" + director + "','" + price + "','" + star
                                + "'," + birthYear + ",'" + genre + "');";
                    }
                    System.out.println(query);
                    PreparedStatement statement = conn.prepareStatement(query);
                    ResultSet rs = statement.executeQuery(query);
                    rs.close();
                    statement.close();
                    break;
                }
                movieExistsRs.close();
                movieExistsStatement.close();
            }
            out.write(jsonObject.toString());
            response.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
