import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String salesId = request.getParameter("salesId");
        String copiesQuery = "SELECT copiesPurchasedIds FROM sales WHERE id= ?";
        String infoQuery = "SELECT copies, movie_id, priceForEach, title FROM copies_purchased, movies WHERE copies_purchased.id= ? AND copies_purchased.movie_id=movies.id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement copiesStatement = conn.prepareStatement(copiesQuery);
             PreparedStatement infoStatement = conn.prepareStatement(infoQuery);
        ){
            conn.setAutoCommit(false);
            JsonArray movieTitleJsonArray = new JsonArray();
            JsonArray countJsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            copiesStatement.setString(1, salesId);
            ResultSet copiesRs = copiesStatement.executeQuery();
            String copiesPurchasedIds = "";
            while (copiesRs.next()) {
                copiesPurchasedIds = copiesRs.getString("copiesPurchasedIds");
            }
            String copiesPurchasedIdsArray[] = copiesPurchasedIds.replaceAll("[\\[\\],\"\\s+]", " ").trim().split("\\s+");


            double total_price = 0.0;
            for (String s : copiesPurchasedIdsArray) {
                infoStatement.setString(1, s);
                ResultSet infoRs = infoStatement.executeQuery();

                while (infoRs.next()) {
                    movieTitleJsonArray.add(infoRs.getString("title"));
                    countJsonArray.add(infoRs.getInt("copies"));
                    total_price += Math.round(infoRs.getInt("copies") *
                            Float.parseFloat(infoRs.getString("priceForEach")) *
                            100.0) / 100.0;
                }

                infoRs.close();
            }

            jsonObject.add("movie_titles", movieTitleJsonArray);
            jsonObject.add("counts", countJsonArray);
            jsonObject.addProperty("total_price", (Math.round(total_price * 100.0) / 100.0) + "");

            copiesRs.close();
            conn.commit();
            conn.setAutoCommit(true);
            out.write(jsonObject.toString());
            response.setStatus(200);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}