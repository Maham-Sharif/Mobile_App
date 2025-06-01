package com.example.phonely;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView phonesRecyclerView;
    List<PhoneModel> phoneList = new ArrayList<>();
    PhoneAdapter adapter;
    FirebaseDatabase database;
    DatabaseReference phoneRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phonesRecyclerView = findViewById(R.id.phonesRecyclerView);
        phonesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PhoneAdapter(phoneList, this);
        phonesRecyclerView.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                boolean isLoggedIn = getSharedPreferences("PhonelyPrefs", MODE_PRIVATE)
                        .getBoolean("isLoggedIn", false);

                if (isLoggedIn) {
                    fetchUserProfileAndOpenProfileActivity();
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
            }
            return false;
        });

        // TODO: Load phones here from Firebase if needed
    }

    private void fetchUserProfileAndOpenProfileActivity() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance(
                        "https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String profileUrl = "";
                if (snapshot.exists() && snapshot.child("profileUrl").exists()) {
                    profileUrl = snapshot.child("profileUrl").getValue(String.class);
                }

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("profileUrl", profileUrl);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }
}
