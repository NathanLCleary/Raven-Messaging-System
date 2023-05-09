package com.example.ravensub1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class SettingsActivity extends AppCompatActivity {

    EditText oldUsername;
    EditText oldPassword;
    EditText newUsername;
    EditText newPassword;
    TextView displayMessage;
    String newUsernameString;
    String newPasswordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        oldUsername = findViewById(R.id.old_usernameET);
        oldPassword = findViewById(R.id.old_passwordET);
        newUsername = findViewById(R.id.new_usernameET);
        newPassword = findViewById(R.id.new_passwordET);
        displayMessage = findViewById(R.id.displayError1);

    }

    public void update(View view) throws InterruptedException {
        String oldUsernameString = oldUsername.getText().toString();
        String oldPasswordString = oldPassword.getText().toString();
        newUsernameString = newUsername.getText().toString();
        newPasswordString = newPassword.getText().toString();

        if(oldUsernameString.equals("") || oldPasswordString.equals("") || newUsernameString.equals("") || newPasswordString.equals("")){
            displayMessage.setText("You cannot have empty fields");
            return;
        }

        final String[] s = new String[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // see if old username and password is correct

                    // URL to sign in function
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

                    Thread thread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                                    // update the username and password

                                    Thread thread3 = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                updateDatabase(s[0].substring(3));
                                            } catch (IOException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread3.start();
                                    try {
                                        thread3.join();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    // something went wrong
                                    displayMessage.setText("An unknown error occurred");
                                    break;
                            }
                        }
                    });
                    runOnUiThread(thread2);

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

    }

    public void updateDatabase(String id) throws IOException, InterruptedException {
        // sign up
        final String[] s = new String[1];


        // URL to update function
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
        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                switch (s[0]) {
                    case "403":
                        // user already exists
                        displayMessage.setText("User already exists, select a different username or sign in");
                        break;
                    case "404":
                        // something went wrong
                        displayMessage.setText("Something went wrong");
                        break;
                    case "200":
                        // user was created
                        displayMessage.setText("User created successfully");

                        stopService();

                        Intent mainScreen = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(mainScreen);

                        break;
                    default:
                        // something went wrong
                        displayMessage.setText("An unknown error occurred");
                        break;
                }
            }
        });
        thread4.start();
        thread4.join();

    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        stopService(serviceIntent);
    }

    public void cancel(View view){
        Intent intent = new Intent(this, ShowMessagesActivity.class);
        String currentTopic = BackgroundService.getTopic();
        intent.putExtra("topic", currentTopic);
        startActivity(intent);
    }
}