package com.example.ravensub1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class ShowMessagesActivity extends AppCompatActivity {
    private String data;
    private TextView textView;
    String username;
    String message;
    String topic;
    Date date;
    int idInt;
    Activity main;

    boolean color = true;
    ArrayList<MessageObject> messageArray= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //textView = findViewById(R.id.TextView);
        main = this;

        username = BackgroundService.getTopic();
        TextView welcomeTV = findViewById(R.id.welcomeText);
        welcomeTV.setText("Welcome " + username);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    //Your code goes here
                    Log.i("Testing", "on create: getting data from database");

                    // URL to get messages function
                    URL githubEndpoint =
                            new URL("");
                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) githubEndpoint.openConnection();

                    Log.i("Testing", "Getting Data");

                    Scanner scanner = new Scanner(myConnection.getInputStream());

                    while(scanner.hasNext()) {
                        String s = scanner.nextLine();

                        data = data + " " + s;
                        //Log.i("Testing", "Getting Data" + scanner.nextLine());
                    }
                    String[] arrOfStr = data.split(",");

                    Log.i("Testing", "Array" + Arrays.toString(arrOfStr));


                    MessageObject m = null;
                    for (int i = 0; i < arrOfStr.length - 1; i++) {

                        String s = arrOfStr[i];
                        //Log.i("Testing", "Position of array: " + i);
                        s = s.replace("\\[", "");
                        s = s.replace("]", "");
                        s = s.replace("}", "");
                        s = s.replace("{", "");

                        //Log.i("Testing", "i: " + i%4);

                        if (i % 4 == 3) {
                            message = s.substring((s.indexOf(':') + 3), (s.length() - 4));
                            //Log.i("Testing", "message: " + message);
                            m.setMessage(message);
                            messageArray.add(m);
                        } else if (i % 4 == 2) {
                            topic = s.substring((s.indexOf(':') + 3), (s.length() - 1));
                            m.setTopic(topic);
                            //Log.i("Testing", "topic: " + topic);
                        } else if (i % 4 == 1) {
                            String sDate = s.substring((s.indexOf(':') + 3), (s.length() - 1));

                            //Log.i("Test", "run: " + sDate);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm:ss.SSS['Z']")
                                    .withZone(ZoneOffset.UTC);
                            OffsetDateTime d = OffsetDateTime.parse(sDate, formatter);
                            date = Date.from(d.toInstant());
                            m.setDate(date);
                            //Log.i("Testing", "run: " + date.toString());
                            // Log.i("Testing", "run: " + date.getYear());
                            //Log.i("Testing", "date: " + date);
                        } else {
                            m = new MessageObject();
                            idInt = Integer.parseInt(s.substring(s.indexOf(':') + 2));
                            m.setId(idInt);
                            //Log.i("Testing", "Id: " + idInt);
                        }

                        //Log.i("Testing", "Updating table " + idInt);


                        //runnable.wait();
                        //Log.i("Testing", "Moving on " + idInt);
                    }
                    for (MessageObject o: messageArray) {
                        System.out.println(o.getMessage());
                    }

                    synchronized (runnable) {
                        runOnUiThread(runnable);
                        runnable.wait(); // unlocks runnable while waiting
                    }
                    myConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();
    }

    Runnable runnable = new Runnable() {
            @Override
            public void run() {
                displayTable();
               // Log.i("Testing", "Thread is finished " + idInt);

                synchronized(this)
                {
                    this.notify();
                }
            }
        };

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        //popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.menu);

        popup.show();
    }
    public void onGroupItemClick(MenuItem item) {
        // One of the group items (using the onClick attribute) was clicked.
        // The item parameter passed here indicates which item it is.
        // All other menu item clicks are handled by Activity.onOptionsItemSelected.


        if(item.getItemId() == R.id.connection){
            if(isMyServiceRunning()){
                stopService();
            }else{
                Intent serviceIntent = new Intent(this, BackgroundService.class);
                serviceIntent.putExtra("topic", username);
                startService(serviceIntent);
            }
        }
        else if(item.getItemId() == R.id.logout){
            stopService();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.updateInfo){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
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

    public void displayTable(){
        TableLayout stk = (TableLayout) findViewById(R.id.table_main);  //Table layout

        stk.removeAllViews();
        //RelativeLayout item = (RelativeLayout)findViewById(R.id.item);
        View child = getLayoutInflater().inflate(R.layout.table_row, null);

        TextView dateTV = child.findViewById(R.id.column1);
        TextView messageTV = child.findViewById(R.id.column2);
        dateTV.setText(" Date ");
        messageTV.setText(" Message ");

        dateTV.setGravity(Gravity.CENTER);
        dateTV.setTypeface(null, Typeface.BOLD);
        dateTV.setTextSize(20);

        messageTV.setGravity(Gravity.CENTER);
        messageTV.setTypeface(null, Typeface.BOLD);
        messageTV.setTextSize(20);

        stk.addView(child);

        EditText searchET = findViewById(R.id.searchText);
        String value = searchET.getText().toString();

        for( int i = 0; i < messageArray.size() - 1; i++) {
            MessageObject o = messageArray.get(i);
            if(o.getMessage().contains(value)){
                View child1 = getLayoutInflater().inflate(R.layout.table_row, null);
                TextView dateTV1 = child1.findViewById(R.id.column1);
                TextView messageTV1 = child1.findViewById(R.id.column2);

                TableRow tbrow = new TableRow(main);

                Date dateDate = o.getDate();
                // get string of date
                int day = dateDate.getDate();
                int month = dateDate.getMonth() + 1;
                int year = dateDate.getYear() + 1900;
                int hour = dateDate.getHours();
                int min = dateDate.getMinutes();

                //Log.i("Testing", "displayTable: " + min);
                String sDate;
                if (min <= 9) {
                    sDate = day + "/" + month + "/" + year + " " + hour + ":" + "0" + min;
                    //Log.i("Testing", "displayTable: 0" + min);
                } else {
                    sDate = day + "/" + month + "/" + year + " " + hour + ":" + min;
                }

                dateTV1.setText(sDate);
                messageTV1.setText(o.getMessage());

                stk.addView(child1);
            }

        }
    }

    public void search(View view) {
        displayTable();
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        stopService(serviceIntent);
    }
}