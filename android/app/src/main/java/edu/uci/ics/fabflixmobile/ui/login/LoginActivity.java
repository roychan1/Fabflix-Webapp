package edu.uci.ics.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;
import com.google.gson.*;

import edu.uci.ics.fabflixmobile.Constants;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.main.MainActivity;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */

    private final String baseURL = "http://" + Constants.host + ":" + Constants.port + "/" + Constants.domain;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CookieHandler.getDefault() instanceof CookieManager) {
            CookieStore cookieStore = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
            String username = "", password = "";
            for (HttpCookie c : cookieStore.getCookies()) {
                if (c.getName().equals("Fabflix_username")) {
                    username = c.getValue();
                } else if (c.getName().equals("Fabflix_password")) {
                    password = c.getValue();
                }
            }
//            System.out.println("USERNAME FROM COOKIE: " + username);
//            System.out.println("PASSWORD FROM COOKIE: " + password);
            finish();
            Intent MainPage = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(MainPage);
        }
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();
                    if (responseObject.get("status").getAsString().equals("success")) {
                        Log.d("login.success", response);
                        // might not be secure to store credentials on cookies directly, could store sessionid?
                        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(null, new HttpCookie("Fabflix_username", username.getText().toString()));
                        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(null, new HttpCookie("Fabflix_password", password.getText().toString()));
                        //Complete and destroy login activity once successful
                        finish();
//                        // initialize the activity(page)/destination
//                        Intent MovieListPage = new Intent(LoginActivity.this, MovieListActivity.class);
//                        // activate the list page.
//                        startActivity(MovieListPage);
                        Intent MainPage = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(MainPage);

                    } else {
                        Log.d("login.fail", response);
                        message.setText("Incorrect username or password");
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                    message.setText("Login failed");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("mobile", "true");
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}