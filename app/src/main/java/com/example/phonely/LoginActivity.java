package com.example.phonely;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (email.contains("admin")) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(this, CustomerMainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
