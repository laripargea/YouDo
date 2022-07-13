package com.example.youdo;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.youdo.ui.ReminderBroadcast;
import com.example.youdo.ui.ReminderBroadcastDaily;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    TextView textView2;
    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView listView;
    Button addTasksButton;
    EditText input;
    String textTask = "";
    ArrayList<String> itemsList = new ArrayList<>();
    Button export;
    Button sortTasksButton;
    Button filterTasksButton;
    SearchView searchbarFilter;
    Button refreshButton;
    ArrayList<String> tasksReloaded = new ArrayList<>();
    Button sendNotification;
    // declaring width and height for our PDF file.
    int pageHeight = 350;
    int pagewidth = 200;
    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //channels for notifications
        createNotificationChannel();
        createNotificationChannelDaily();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_tasks);
        textView2 = findViewById(R.id.textView2);
        listView = findViewById(R.id.listView);
        addTasksButton = findViewById(R.id.addTaskButton);
        input = findViewById(R.id.newTaskText);
        export = findViewById(R.id.button3);
        sortTasksButton = findViewById(R.id.buttonSort);
        filterTasksButton = findViewById(R.id.buttonFilter);
        searchbarFilter = findViewById(R.id.searchView);
        refreshButton = findViewById(R.id.buttonRefresh);
        sendNotification = findViewById(R.id.buttonNotifications);

        // keeping user logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //initialize list with tasks
            items = new ArrayList<>();

            addTasksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addItem(view);
                }
            });

            sortTasksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("TASKS: " + items);
                    ArrayList<String> sortedTasks = new ArrayList<>(items);
                    Collections.sort(sortedTasks, String.CASE_INSENSITIVE_ORDER);
                    System.out.println("TASKS SORTED: " + sortedTasks);
                    items.clear();
                    for (String s : sortedTasks) {
                        items.add(s);
                    }
                    listView.setAdapter(itemsAdapter);
                    itemsAdapter.notifyDataSetChanged();
                }
            });

            searchbarFilter.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listView.setVisibility(View.GONE);
                    sortTasksButton.setVisibility(View.GONE);
                }
            });

            filterTasksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listView.setVisibility(View.VISIBLE);
                    sortTasksButton.setVisibility(View.VISIBLE);
                    System.out.println("TASKS: " + items);

                    CharSequence searchbarFilterText = searchbarFilter.getQuery();
                    System.out.println("This is the text in the searchbar: " + searchbarFilterText);

                    ArrayList<String> filteredTasks = new ArrayList<>();
                    for (String s : items) {
                        if (s.contains(searchbarFilterText)) {
                            filteredTasks.add(s);
                        }
                    }
                    if (filteredTasks.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No task with this content was found :(", Toast.LENGTH_LONG).show();
                    }
                    System.out.println("TASKS FILTERED: " + filteredTasks);
                    items.clear();
                    for (String s : filteredTasks) {
                        items.add(s);
                    }
                    listView.setAdapter(itemsAdapter);
                    itemsAdapter.notifyDataSetChanged();
                }
            });

            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listView.setVisibility(View.VISIBLE);
                    sortTasksButton.setVisibility(View.VISIBLE);
                    System.out.println("List with tasks refreshed successfully");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        //read data from database
                        DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        //reading tasks from db + put them in the tasks list -> sa le avem mereu in pag fiecarui user
                                        tasksReloaded = (ArrayList<String>) document.get("tasks");
                                        items.clear();
                                        for (String s : tasksReloaded) {
                                            items.add(s);
                                        }
                                        listView.setAdapter(itemsAdapter);
                                        itemsAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                }
            });

            sendNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //notifications - Remind me in x hours/minutes/seconds
                    Toast.makeText(MainActivity.this, "Reminder was set successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    long timeAtButtonClick = System.currentTimeMillis();
                    long tenSecondsInMillis = 1000 * 10;
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSecondsInMillis, pendingIntent);

                    //daily notifications
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 12);
                    calendar.set(Calendar.MINUTE, 30);
                    Intent intentDailyNotifications = new Intent(MainActivity.this, ReminderBroadcastDaily.class);
                    PendingIntent pendingIntentDailyNotifications = PendingIntent.getBroadcast(getApplicationContext(), 100, intentDailyNotifications, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManagerDailyNotifications = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManagerDailyNotifications.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntentDailyNotifications);
                }
            });

            itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            setUpListViewListener();
        } else {
            textView2.setText("No user logged in");
            addTasksButton.setVisibility(View.GONE);
            sortTasksButton.setVisibility(View.GONE);
            filterTasksButton.setVisibility(View.GONE);
            searchbarFilter.setVisibility(View.GONE);
            refreshButton.setVisibility(View.GONE);
            input.setVisibility(View.GONE);
            export.setVisibility(View.GONE);
            sendNotification.setVisibility(View.GONE);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_tasks:
                        return true;
                    case R.id.navigation_account:
                        if (user != null) {
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_signup:
                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        // below code is used for checking our permissions.
        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling method to generate our PDF file.
                generatePDF();
            }
        });
    }

    private void generatePDF() {
        // creating an object variable for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // we will use "title" for adding text in our PDF file.
        Paint title = new Paint();

        // we are adding page info to our PDF file in which we will be passing our pageWidth, pageHeight and number of pages and after that we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();

        // below line is used for setting start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used for adding typeface for our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // below line is used for setting text size which we will be displaying in our PDF file.
        title.setTextSize(15);

        // below line is sued for setting color of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));

        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));
        title.setTextSize(15);

        canvas.drawText("Here are your tasks:", 30, 30, title);
        title.setTextAlign(Paint.Align.LEFT);
        int y = 80;
        for (String task : items) {
            canvas.drawText("- " + task, 20, y, title);
            y += 20;
        }

        // after adding all attributes to our PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of our PDF file and its path.
        File file = new File(Environment.getExternalStorageDirectory(), "MyTasks.pdf");

        try {
            // after creating a file name we will write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(file));

            // below line is to print toast message on completion of PDF generation.
            Toast.makeText(MainActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // below line is used to handle error
            e.printStackTrace();
        }
        // after storing our pdf to that location we are closing our PDF file.
        pdfDocument.close();
    }

    private boolean checkPermission() {
        // checking for permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denined.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //read data from database
            DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String tempString = document.getData().get("fullname").toString();
                            textView2.setText(tempString.split(" ")[0] + "'s Tasks List");

                            //reading tasks from db + put them in the tasks list
                            itemsList = (ArrayList<String>) document.get("tasks");
                            if(itemsList==null){
                                itemsList = new ArrayList<>();
                            }
                            for (String s : itemsList) {
                                items.add(s);
                            }
                            listView.setAdapter(itemsAdapter);
                            itemsAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    void setUpListViewListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Task removed", Toast.LENGTH_LONG).show();
                String messageToBeShared = "I successfully finished the following task '" + items.get(i) + "' from my YouDo-list!";

                //delete item from the database
                DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
                docRef.update("tasks", FieldValue.arrayRemove(items.get(i)));
                //delete item from list
                items.remove(i);
                itemsAdapter.notifyDataSetChanged();

                //share message to another app
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, messageToBeShared);
                shareIntent.setType("text/plain");
                //startActivity to the other app, message will be copied directly to the app
                if(shareIntent.resolveActivity(getPackageManager())!=null){
                    startActivity(shareIntent);
                }
            }
        });
    }

    void addItem(View view) {
        String itemText = input.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!(itemText.equals(""))) {
            itemsAdapter.add(itemText);
            textTask = itemText;
            input.setText("");

            //add item to the database
            DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
            docRef.update("tasks", FieldValue.arrayUnion(itemText));
        } else {
            Toast.makeText(getApplicationContext(), "Please enter task", Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "YouDoReminderChannel";
            String description = "Channel for YouDo App Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyYouDo", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationChannelDaily(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "YouDoReminderChannelDaily";
            String description = "Channel for YouDo App Reminder Daily";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("dailyNotification", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}