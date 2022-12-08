import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.json.Json;

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
import java.util.HashMap;
import java.util.Map;



@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private DataSource dataSource;
    private final String QUERY = "SELECT title, priceForEach FROM movies WHERE id= ? ";


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY);
        ){

            HttpSession session = request.getSession();
            ArrayList<CartItem> itemsArray = (ArrayList<CartItem>) session.getAttribute("cart_items");
            if (itemsArray == null) {
                itemsArray = new ArrayList<CartItem>();
            }

            JsonArray itemsJsonArray = new JsonArray();
            for (int i = 0; i < itemsArray.size(); i++) {
                String id = itemsArray.get(i).getId();
                int count = itemsArray.get(i).getCount();
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    JsonObject itemJsonObject = new JsonObject();
                    itemJsonObject.addProperty("title", rs.getString("title"));
                    itemJsonObject.addProperty("id", id);
                    itemJsonObject.addProperty("priceForEach", rs.getString("priceForEach"));
                    itemJsonObject.addProperty("count", count);
                    itemsJsonArray.add(itemJsonObject);
                }
                rs.close();
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("cart_items", itemsJsonArray);
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

    /**
     * Required POST data/ parameter: id,
     * Optional POST data/ parameter: remove, quantity
     * @param request
     * @param response
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String id = request.getParameter("id");

        HttpSession session = request.getSession();
        ArrayList<CartItem> itemsArray = (ArrayList<CartItem>) session.getAttribute("cart_items");
        if (itemsArray == null) {
            itemsArray = new ArrayList<CartItem>();
            itemsArray.add(new CartItem(id));
            session.setAttribute("cart_items", itemsArray);
        } else {
            String remove = request.getParameter("remove");
            String quantity = request.getParameter("quantity");
            SYNCHRONIZED:
            synchronized (itemsArray) {
                for (CartItem item : itemsArray) {
                    if (item.getId().equals(id)) {
                        if (remove != null && remove.equals("true")) {
                            itemsArray.remove(item);
                        } else if (quantity != null) {
                            item.setCount(Integer.parseInt(quantity));
                        } else {
                            item.incrementCount();
                        }
                        break SYNCHRONIZED;
                    }
                }

                itemsArray.add(new CartItem(id));
            }
        }

        JsonArray jsonArray = new JsonArray();
        try (Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY);
        ) {
            for (int i = 0; i < itemsArray.size(); i++) {
                String itemId = itemsArray.get(i).getId();
                int itemCount = itemsArray.get(i).getCount();

                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    JsonObject itemJsonObject = new JsonObject();
                    itemJsonObject.addProperty("title", rs.getString("title"));
                    itemJsonObject.addProperty("id", itemId);
                    itemJsonObject.addProperty("priceForEach", rs.getString("priceForEach"));
                    itemJsonObject.addProperty("count", itemCount);
                    jsonArray.add(itemJsonObject);
                }
                rs.close();
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("cart_items", jsonArray);
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