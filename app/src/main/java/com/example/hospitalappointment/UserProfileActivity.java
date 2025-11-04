package com.example.hospitalappointment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivProfilePicture;
    private EditText etUserName, etUserEmail, etUserPhone;
    private Button btnSave, btnLogOut;
    private LinearLayout navHome, navInbox, navUser;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load user data
        loadUserData();

        // Edge-to-edge with proper system bar insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply padding only to the main container to avoid content hiding behind system bars
            View mainContainer = findViewById(R.id.main);
            if (mainContainer != null) {
                mainContainer.setPadding(
                    Math.max(systemBars.left, 0),
                    Math.max(systemBars.top, 0),
                    Math.max(systemBars.right, 0),
                    Math.max(systemBars.bottom, 0)
                );
            }
            
            return insets;
        });
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        etUserName = findViewById(R.id.etUserName);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPhone = findViewById(R.id.etUserPhone);
        btnSave = findViewById(R.id.btnSave);
        btnLogOut = findViewById(R.id.btnLogOut);

        // Initialize navigation buttons
        navHome = findViewById(R.id.navHome);
        navInbox = findViewById(R.id.navInbox);
        navUser = findViewById(R.id.navUser);
    }

    private void setupClickListeners() {
        // Back button click listener
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Save button click listener
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        // Log out button click listener
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Navigation button click listeners
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        navInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, AppointmentHistoryActivity.class);
                startActivity(intent);
            }
        });

        navUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on User Profile page
                Toast.makeText(UserProfileActivity.this, "You are already on User Profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Load user data from Firebase Auth
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();
            
            if (displayName != null && !displayName.isEmpty()) {
                etUserName.setText(displayName);
            } else {
                // Extract name from email if display name is not available
                if (email != null && email.contains("@")) {
                    String name = email.substring(0, email.indexOf("@"));
                    name = name.replace(".", " ");
                    name = capitalizeWords(name);
                    etUserName.setText(name);
                } else {
                    etUserName.setText("User");
                }
            }
            
            etUserEmail.setText(email != null ? email : "");
            etUserPhone.setText("+91 99999 99999"); // Default phone number
            
            // Load additional data from Firebase Database
            loadUserDataFromDatabase();
        } else {
            // User not logged in
            etUserName.setText("Guest User");
            etUserEmail.setText("");
            etUserPhone.setText("");
        }
    }
    
    private void loadUserDataFromDatabase() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        
                        if (name != null && !name.isEmpty()) {
                            etUserName.setText(name);
                        }
                        if (phone != null && !phone.isEmpty()) {
                            etUserPhone.setText(phone);
                        }
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    // Handle error silently
                }
            });
        }
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    private void saveUserData() {
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String name = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        
        // Validate input
        if (name.isEmpty()) {
            etUserName.setError("Name is required");
            etUserName.requestFocus();
            return;
        }
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUserEmail.setError("Valid email is required");
            etUserEmail.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            etUserPhone.setError("Phone number is required");
            etUserPhone.requestFocus();
            return;
        }
        
        // Show loading
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");
        
        // Update Firebase Database
        if (userRef != null) {
            userRef.child("name").setValue(name);
            userRef.child("phone").setValue(phone);
            userRef.child("email").setValue(email);
        }
        
        // Update Firebase Auth profile
        if (currentUser != null) {
            com.google.firebase.auth.UserProfileChangeRequest profileUpdates = 
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
                    
            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    private void logoutUser() {
        if (currentUser != null) {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate to login screen
            Intent intent = new Intent(UserProfileActivity.this, LoginSignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate back to home or previous activity
        Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
