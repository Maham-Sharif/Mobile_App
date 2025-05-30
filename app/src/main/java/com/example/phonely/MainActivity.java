package com.example.phonely;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonely.PhoneAdapter;     // Your custom adapter
import com.example.phonely.PhoneModel;         // Your phone model class
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;

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
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            }
            return false;
        });

        database = FirebaseDatabase.getInstance();
        phoneRef = database.getReference("phones");

        phoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                phoneList.clear();
                for (DataSnapshot phoneSnap : snapshot.getChildren()) {
                    PhoneModel phone = phoneSnap.getValue(PhoneModel.class);
                    phoneList.add(phone);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerMainActivity.this, "Error loading phones", Toast.LENGTH_SHORT).show();
            }
        });
    }
}