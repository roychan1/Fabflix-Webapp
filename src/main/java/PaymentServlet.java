import com.google.gson.JsonObject;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String query = "SELECT priceForEach FROM movies WHERE id= ? ";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
        ) {

            HttpSession session = request.getSession();
            ArrayList<CartItem> itemsArray = (ArrayList<CartItem>) session.getAttribute("cart_items");
            JsonObject jsonObject = new JsonObject();
            if (itemsArray == null) {
                jsonObject.addProperty("total_price", 0);
                out.write(jsonObject.toString());
                return;
            }

            float total_price = (float)0;
            for (CartItem item : itemsArray) {
                statement.setString(1, item.getId());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    total_price += Float.parseFloat(rs.getString("priceForEach")) * item.getCount();
                }
                rs.close();
            }

            jsonObject.addProperty("total_price", (Math.round(total_price * 100.0) / 100.0) + "");
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


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("credit_card_number");
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String expirationDate = request.getParameter("expiration_date");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String copiesPurchasedUpdate = "INSERT INTO copies_purchased VALUES(?, ? , ? )";
        String customerIdQuery = "SELECT id FROM customers WHERE email= ? ";
        String creditcardsQuery = "SELECT * FROM creditcards WHERE id= ? ";
        String salesUpdate = "INSERT INTO sales VALUES(null, ? , ? , ? )";
        String salesCountQuery = "SELECT COUNT(*) FROM sales";

        try {
            writeDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-write");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = writeDataSource.getConnection();
            PreparedStatement customerIdStatement = conn.prepareStatement(customerIdQuery);
            PreparedStatement creditcardsStatement = conn.prepareStatement(creditcardsQuery);
            PreparedStatement salesStatement = conn.prepareStatement(salesUpdate);
            PreparedStatement salesCountStatement = conn.prepareStatement(salesCountQuery);
        ) {

            HttpSession session = request.getSession();
            ArrayList<CartItem> itemsArray = (ArrayList<CartItem>) session.getAttribute("cart_items");
            String copiesPurchasedIds = "[";

            Integer copy_id = null;
            String query_copy_id = "SELECT MAX(id) as copy_id FROM copies_purchased;";
            PreparedStatement copyStatement = conn.prepareStatement(query_copy_id);
            ResultSet copyRs = copyStatement.executeQuery(query_copy_id);
            while (copyRs.next()) {
                copy_id = copyRs.getInt("copy_id");
            }
            copyRs.close();
            copyStatement.close();

            for (int i = 0; i < itemsArray.size(); i++) {
                copy_id += 1;
                PreparedStatement copiesPurchasedStatement = conn.prepareStatement(copiesPurchasedUpdate);
                copiesPurchasedStatement.setInt(1, copy_id);
                copiesPurchasedStatement.setString(2, itemsArray.get(i).getId());
                copiesPurchasedStatement.setInt(3, itemsArray.get(i).getCount());
                copiesPurchasedStatement.executeUpdate();
                copiesPurchasedStatement.close();

                copiesPurchasedIds += "\"" + copy_id + "\",";
            }
            copiesPurchasedIds = copiesPurchasedIds.substring(0,copiesPurchasedIds.length()-1);
            copiesPurchasedIds += "]";

            User user = (User)session.getAttribute("user");
            customerIdStatement.setString(1, user.getUsername());
            ResultSet customerIdRs = customerIdStatement.executeQuery();
            int userId = 0;
            while (customerIdRs.next()) {
                userId = customerIdRs.getInt("id");
            }
            customerIdRs.close();

            System.out.println("About to confirm...");
            creditcardsStatement.setString(1, id);
            ResultSet creditcardsRs = creditcardsStatement.executeQuery();
            while (creditcardsRs.next()) {
                System.out.println("Confirming...");
                if (creditcardsRs.getString("id").equals(id) &&
                        creditcardsRs.getString("firstName").equalsIgnoreCase(firstName) &&
                        creditcardsRs.getString("lastName").equalsIgnoreCase(lastName) &&
                        creditcardsRs.getString("expiration").equals(expirationDate)) {
                    System.out.println(creditcardsRs.getString("id"));
                    salesStatement.setInt(1, userId);
                    salesStatement.setString(2, copiesPurchasedIds);
                    salesStatement.setString(3, "" + java.time.LocalDate.now());
                    salesStatement.executeUpdate();

                    ResultSet salesCountRs = salesCountStatement.executeQuery();
                    JsonObject jsonObject = new JsonObject();
                    while (salesCountRs.next()) {
                        jsonObject.addProperty("sales_id", salesCountRs.getInt(1));
                    }
                    salesCountRs.close();

                    session.setAttribute("cart_items", null);

                    out.write(jsonObject.toString());
                    response.setStatus(200);
                    return;
                }
            }
            creditcardsRs.close();

            response.setStatus(500);
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
