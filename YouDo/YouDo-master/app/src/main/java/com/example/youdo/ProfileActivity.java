package com.example.youdo;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profilePic;
    public Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    TextView email;
    BottomNavigationView bottomNavigationView;
    Button saveButton;
    EditText firstName;
    EditText lastName;
    EditText dateInput;
    String firstNameDB;
    String lastNameDB;
    String emailDB;
    String dateDB;
    Button logoutButton;
    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    String firstnameToBeSent;
    String lastnameToBeSent;
    String birthdayToBeSent;
    ArrayList<String> tasks;
    Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        profilePic = findViewById(R.id.imageView);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        profilePic.setImageResource(R.drawable.profile_pic);
        email = (TextView) findViewById(R.id.editTextTextEmailAddress);
        saveButton = (Button) findViewById(R.id.button2);
        logoutButton = (Button) findViewById(R.id.buttonLogout);
        firstName = (EditText) findViewById(R.id.editTextTextPersonName);
        lastName = (EditText) findViewById(R.id.editTextTextPersonName2);
        dateInput = (EditText) findViewById(R.id.editTextDate);
        resetPasswordButton = findViewById(R.id.buttonResetPassword);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            System.out.println("User logged: " + user.getEmail());
            //read data from database
            DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String tempString = document.getData().get("fullname").toString();
                            String[] firstNameAndLastName = tempString.split(" ");
                            List<String> separatedFirstNameAndLastName = new ArrayList<>();
                            for (String name : firstNameAndLastName) {
                                separatedFirstNameAndLastName.add(name);
                            }
                            firstnameToBeSent = separatedFirstNameAndLastName.get(0);
                            lastnameToBeSent = separatedFirstNameAndLastName.get(1);
                            birthdayToBeSent = document.getData().get("birthday").toString();
                            email.setText(user.getEmail());
                            email.setFocusable(false);
                            firstName.setText(firstnameToBeSent);
                            lastName.setText(lastnameToBeSent);
                            dateInput.setText(birthdayToBeSent);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        } else {
            System.out.println("User logged out");
        }

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_account);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_tasks:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_account:
                        return true;
                    case R.id.navigation_signup:
                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        //LOG OUT
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //logout user
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ProfileActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                dateInput.setText(date);
            }
        };

        //reading tasks from db to put them in the updated user
        if (user != null) {
            //read data from database
            DocumentReference docRef = firestoreDB.collection("users").document(user.getEmail());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            tasks = (ArrayList<String>) document.get("tasks");
                            System.out.println(tasks);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstNameDB = firstName.getText().toString();
                lastNameDB = lastName.getText().toString();
                emailDB = email.getText().toString();
                dateDB = dateInput.getText().toString();

                // Create a Map to store the data we want to set
                Map<String, Object> user = new HashMap<>();
                user.put("fullname", firstNameDB + " " + lastNameDB);
                user.put("birthday", dateDB);
                user.put("tasks", tasks);

                // Add a new document with a generated ID
                firestoreDB.collection("users").document(emailDB)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void avoid) {
                                Log.d("Successfully added", "DocumentSnapshot successfully added with ID: " + email);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("NOT successfully added", "Error adding document", e);
                            }
                        });
                Toast.makeText(getApplicationContext(), "Your changes have been made!", Toast.LENGTH_LONG).show();

            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("This is the reset Password Button");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Reset email sent successfully!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed To Upload", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Progress: " + (int) progressPercent + "%");
            }
        });
    }
}