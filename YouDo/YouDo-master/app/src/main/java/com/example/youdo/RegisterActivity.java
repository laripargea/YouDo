package com.example.youdo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseFirestore firestoreDB;
    String fullname;
    String email;
    String password;
    String confirmPassword;
    EditText fullnameInput;
    EditText emailInput;
    EditText passwordInput;
    EditText confirmPasswordInput;
    Button registerButton;
    BottomNavigationView bottomNavigationView;
    Button sendFeedback;
    TextInputEditText feedback;
    ArrayList<String> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fullnameInput = (EditText) findViewById(R.id.fullnameInput);
        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        confirmPasswordInput = (EditText) findViewById(R.id.confirmPasswordInput);
        registerButton = (Button) findViewById(R.id.signupButton);
        sendFeedback = findViewById(R.id.button4);
        feedback = findViewById(R.id.feedback);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        firestoreDB = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullname = fullnameInput.getText().toString();
                email = emailInput.getText().toString();
                password = passwordInput.getText().toString();
                confirmPassword = confirmPasswordInput.getText().toString();

                Log.d("Fullname From User", "Fullname From User taken with success");
                System.out.println(fullname);

                Log.d("Email From User", "Email From User taken with success");
                System.out.println(email);

                Log.d("Password From User", "Password From User taken with success");
                System.out.println(password);

                //REGISTER
                if(password.equals(confirmPassword)){
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        System.out.println("createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        System.out.println(user.getEmail());
                                        Toast.makeText(getApplicationContext(),"You registered successfully!",Toast.LENGTH_LONG).show();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        System.out.println("createUserWithEmail:failure");
                                        System.out.println(task.getException());
                                        Toast.makeText(getApplicationContext(),"You could NOT be registered! " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    // Create a Map to store the data we want to set
                    Map<String, Object> user = new HashMap<>();
                    user.put("fullname", fullname);
                    user.put("birthday", "");
                    user.put("tasks", tasks);

                    // Add a new document with a generated ID
                    firestoreDB.collection("users").document(email)
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
                }
                else{
                    Log.d("Passwords NOT match", "Passwords DO NOT MATCH!");
                }
            }
        });

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_signup);

        //pastrez user-ul logat
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.navigation_tasks:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_account:
                        if(user!=null){
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        }
                        else{
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_signup:
                        return true;
                }
                return false;
            }
        });

        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedbackText = feedback.getText().toString();
                feedback.setText("");
                // Add a new document with a generated id.
                Map<String, Object> feedbackData = new HashMap<>();
                feedbackData.put("text",feedbackText);
                firestoreDB.collection("feedbacks")
                        .add(feedbackData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(),"Feedback successfully sent!",Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Feedback not sent! Try again!",Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}