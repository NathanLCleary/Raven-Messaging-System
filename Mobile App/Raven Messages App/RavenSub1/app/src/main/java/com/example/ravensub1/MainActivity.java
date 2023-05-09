package com.example.ravensub1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    TextView displayMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isMyServiceRunning()) {
            Intent intent = new Intent(this, ShowMessagesActivity.class);
            String currentTopic = BackgroundService.getTopic();
            intent.putExtra("topic", currentTopic);
            startActivity(intent);
        }
        username = findViewById(R.id.UsernameET);
        password = findViewById(R.id.passwordET);
        displayMessage = findViewById(R.id.displayError);

    }
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BackgroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void startService() {
        Intent serviceIntent = new Intent(this, BackgroundService.class);

        String usernameString = username.getText().toString();

        serviceIntent.putExtra("topic", usernameString);
        startService(serviceIntent);

        Intent intent = new Intent(this, ShowMessagesActivity.class);
        intent.putExtra("topic", usernameString);
        startActivity(intent);
    }

    public void logIn(View view) throws InterruptedException {
        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();

        final String[] s = new String[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // URL to Sign in function
                    URL githubEndpoint = new URL("");

                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) githubEndpoint.openConnection();

                    Log.i("Testing", "Getting Data");
                    Scanner scanner = new Scanner(myConnection.getInputStream());

                    s[0] = "402";
                    while(scanner.hasNext()) {
                        s[0] = scanner.nextLine();
                        //Log.i("Testing", "Getting Data" + scanner.nextLine());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //synchronized (thread){
        //    thread.start();
        //}
        thread.start();
        thread.join();
        //thread.wait();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Testing", "logIn: " + s[0].length());
        String error = s[0].substring(0, 3);
        switch (error) {
            case "402":
                // failed to connect to database server
                displayMessage.setText("Failed to connect to database");
                break;
            case "401":
                // wrong password
                displayMessage.setText("Wrong password");
                break;
            case "400":
                // user doesn't exist
                displayMessage.setText("User doesn't exist. Maybe try signing up");
                break;
            case "200":
                // log in successful
                displayMessage.setText("Log in successful");

                startService();
                break;
            default:
                // something went wrong
                displayMessage.setText("An unknown error occurred");
                break;
        }

    }

    public void signUp(View view) throws InterruptedException {
        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();

        final String[] s = new String[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // URL to signin Function 
                    URL githubEndpoint = new URL("");

                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) githubEndpoint.openConnection();

                    Log.i("Testing", "Getting Data");
                    Scanner scanner = new Scanner(myConnection.getInputStream());

                    s[0] = "402";

                    Log.i("Testing", "logIn: " + s[0].length());

                    while(scanner.hasNext()) {
                        s[0] = scanner.nextLine();
                        //Log.i("Testing", "Getting Data" + scanner.nextLine());
                    }
                    String error = s[0].substring(0, 3);
                    // if code is 400 user doesn't exist. We want a 400 so if its not 400 throw error
                    if(!Objects.equals(error, "400")){
                        s[0] = "403";
                        return;
                    }

                    // sign up URL
                    URL githubEndpoint1 = new URL("");

                    HttpsURLConnection myConnection1 =
                            (HttpsURLConnection) githubEndpoint1.openConnection();

                    Log.i("Testing", "Getting Data");
                    Scanner scanner1 = new Scanner(myConnection1.getInputStream());

                    s[0] = "404";
                    while(scanner1.hasNext()) {
                        s[0] = scanner1.nextLine();
                        //Log.i("Testing", "Getting Data" + scanner.nextLine());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //synchronized (thread){
        //    thread.start();
        //}
        thread.start();
        thread.join();
        //thread.wait();

        Log.i("Testing", "signUp: " + s[0]);
        switch (s[0]) {
            case "403":
                // user already exists
                displayMessage.setText("User already exists, select a different username or sign in");
                break;
            case "404":
                // something went wrong
                displayMessage.setText("Something went wrong");
                break;
            case "complete":
                // user was created
                displayMessage.setText("User created successfully");
                password.setText("");
                break;
            default:
                // something went wrong
                displayMessage.setText("An unknown error occurred");
                break;
        }
    }
}