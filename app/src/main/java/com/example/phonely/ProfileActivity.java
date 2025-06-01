package com.example.phonely;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imgProfile, ivUpdate, ivDelete;
    private EditText etEmail, etPassword, etRole;
    private Uri imageUri;

    private Bitmap selectedBitmap; // Store selected bitmap for update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);
        ivUpdate = findViewById(R.id.ivUpdate);
        ivDelete = findViewById(R.id.ivDelete);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRole = findViewById(R.id.etRole);
        etRole.setEnabled(false); // disable editing

        // Pick image on image click
        imgProfile.setOnClickListener(v -> openGallery());
        loadUserProfile();
        // Update logic
        ivUpdate.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Update Profile")
                    .setMessage("Are you sure you want to update your profile?")
                    .setPositiveButton("Yes", (dialog, which) -> updateProfile())
                    .setNegativeButton("No", null)
                    .show();
        });

        // Optional delete
        ivDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteProfile())
                    .setNegativeButton("No", null)
                    .show();
        });

    }
    private void deleteProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Delete profile image from Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId + ".jpg");

        imageRef.delete()
                .addOnSuccessListener(unused -> {
                    // 2. Delete user data from Firebase Realtime Database
                    deleteUserFromDatabase(userId);
                })
                .addOnFailureListener(e -> {
                    // If image not found or deletion failed, still remove user data
                    deleteUserFromDatabase(userId);
                });
    }
    private void deleteUserFromDatabase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance(
                        "https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);

        userRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();

                    // 3. Clear login status in SharedPreferences
                    getSharedPreferences("PhonelyPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLoggedIn", false)
                            .apply();

                    // 4. Sign out from FirebaseAuth
                    FirebaseAuth.getInstance().signOut();

                    // 5. Go back to MainActivity
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete profile data", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);

            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProfile() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance(
                        "https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", email);
        updates.put("password", password);
        updates.put("role", role); // still store same role as before

        // Handle profile image
        if (selectedBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos); // Compress to 20%
            byte[] imageData = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("profile_images/" + userId + ".jpg");

            storageRef.putBytes(imageData).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updates.put("profileUrl", uri.toString());
                    saveToDatabase(userRef, updates);
                });
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveToDatabase(userRef, updates);
        }
    }

    private void saveToDatabase(DatabaseReference userRef, Map<String, Object> updates) {
        userRef.updateChildren(updates)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
    private void loadUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance(
                        "https://phonely-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String email = snapshot.child("email").getValue(String.class);
                String password = snapshot.child("password").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);
                String profileUrl = snapshot.child("profileUrl").getValue(String.class);

                etEmail.setText(email != null ? email : "");
                etPassword.setText(password != null ? password : "");
                etRole.setText(role != null ? role : "");

                if (profileUrl != null && !profileUrl.isEmpty()) {
                    // Load image from URL into imgProfile using your favorite image loader
                    // For example, use Glide (add Glide dependency if not added)
                    Glide.with(this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_person) // your default drawable
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_person);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

}
