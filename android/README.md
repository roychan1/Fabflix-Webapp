# Project 4 Android Example

This is an example Android project to demonstrate how to:

- Change between Android Activities
- Make RESTFul HTTPS requests
- Maintain cookies
- Use ListView with custom row layout
- HTTP is supported by checking /app/src/main/AndroidManifest.xml

## Running this project

This Android app depends on [cs122b-fall22-project2-login-cart-example](https://github.com/uci-jherold2-teaching/cs122b-fall22-project2-login-cart-example) as the backend server to work.

Note: You need to make sure your login cart runs at localhost:8080/cs122b-fall22-project2-login-cart-example(dashes instead of underscores!). 

You can change the url on Intellij IDEA -> Edit Run/Debug Configurations for Tomcat -> Deployment.

![20210510104246](https://user-images.githubusercontent.com/13672781/117701798-81ff8600-b17c-11eb-91e3-76eff6d05831.png)

Follow the instructions on [Canvas](https://canvas.eee.uci.edu/courses/50103/pages/p4-task-2-developing-an-android-app-for-fabflix) to set up the Android App Project

Note: Don't forget to change the URL in Login class to match your deployed backend

## About NukeSSLCerts.java

When you access a website through https, the server will send an SSL certificate to the browser for it to verify. We are using self-signed certificates, which will not be trusted by Android applications by default. The application will throw exceptions once it receives such certificate. NukeSSLCerts.java allows us to bypass SSL by accepting all certificates. If you want to learn more about this, see [this blog](https://www.codeproject.com/Articles/826045/Android-security-Implementation-of-Self-signed-SSL)
