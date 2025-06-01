package com.example.phonely;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Handler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;


public class LoginActivity extends AppCompatActivity {
    ViewFlipper viewFlipper;
    TextView tvGoToRegister, tvGoToLogin;
    EditText editTextEmail, editTextPassword,editTextLoginEmail, editTextLoginPassword;
    RadioButton radioAdmin, radioCustomer;
    Button btnRegister,btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        viewFlipper = findViewById(R.id.viewFlipper);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        tvGoToRegister.setOnClickListener(v -> {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
            viewFlipper.showNext();
        });

        tvGoToLogin.setOnClickListener(v -> {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
            viewFlipper.showPrevious();
        });

        editTextEmail = findViewById(R.id.etNewUsername);
        editTextPassword = findViewById(R.id.etNewPassword);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioCustomer = findViewById(R.id.radioCustomer);
        btnRegister = findViewById(R.id.btnRegister);
        editTextLoginEmail = findViewById(R.id.etUsername);
        editTextLoginPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);


        btnRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String role = "customer";  // Always customer on registration
            if (!email.isEmpty() && !password.isEmpty()) {
                registerUser(email, password, role); // This calls Firebase Authentication
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = editTextLoginEmail.getText().toString().trim();
            String password = editTextLoginPassword.getText().toString().trim();
            String selectedRole = radioAdmin.isChecked() ? "admin" : "customer";
            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password,selectedRole);
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void loginUser(String email, String password, String selectedRole) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
                            DatabaseReference userRef = database.getReference("users").child(uid);

                            userRef.get().addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    if (dbTask.getResult().exists()) {
                                        String storedRole = dbTask.getResult().child("role").getValue(String.class);
                                        if (storedRole != null && storedRole.equals(selectedRole)) {
                                            Toast.makeText(this, "Login successful as " + storedRole, Toast.LENGTH_SHORT).show();

                                            SharedPreferences prefs = getSharedPreferences("PhonelyPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putBoolean("isLoggedIn", true);
                                            editor.putString("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); // or use email
                                            editor.apply();  // or commit()

                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Role mismatch! You selected " + selectedRole + " but your account is " + storedRole, Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        }
                                    } else {
                                        Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to read user data", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser(String email, String password, String role) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save extra user data to Realtime Database
                            saveUserData(user.getUid(), email, role);
                        }
                        Toast.makeText(this, "Registered as " + role, Toast.LENGTH_SHORT).show();
                        // Optional: Redirect after 1 second
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }, 1000);
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private byte[] getCompressedDefaultProfileImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_person);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos); // 20% quality
        return baos.toByteArray();
    }

    private void saveUserData(String userId, String email, String role) {
        byte[] imageData = getCompressedDefaultProfileImage();

        FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg")
                .putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String profileUrl = uri.toString();

                            // Now save user data in Realtime Database
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
                            DatabaseReference usersRef = database.getReference("users");
                            User user = new User(email, role, profileUrl);
                            usersRef.child(userId).setValue(user)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "User saved to database", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}
