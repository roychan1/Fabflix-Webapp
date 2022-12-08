import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        System.out.println("INIT LOGIN SERVLET");
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        PrintWriter out = response.getWriter();
        String customerQuery = String.format("SELECT * from customers where email='%s'", username);

        try (Connection conn = dataSource.getConnection()){
            PreparedStatement statement = conn.prepareStatement(customerQuery);
            ResultSet rs = statement.executeQuery(customerQuery);

            boolean success = false;
            if (rs.next()) {
                // get the encrypted password from the database
                String encryptedPassword = rs.getString("password");

                // use the same encryptor to compare the user input password with encrypted password stored in DB
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
            }

            JsonObject responseJsonObject = new JsonObject();
            if (success) {
                request.getSession().setAttribute("user", new User(username));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }

            if (rs == null || !success) {
                responseJsonObject.addProperty("status", "fail");

                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "Incorrect username or password");
            } else {
                // Verify reCAPTCHA
                if (!(request.getParameter("mobile") != null && request.getParameter("mobile").equals("true"))) {
                    try {
                        RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                    } catch (Exception e) {
                        responseJsonObject.addProperty("status", "fail");

                        request.getServletContext().log("Login failed");
                        responseJsonObject.addProperty("message", "Verify you're not a robot");
                    }
                }
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            out.close();
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}