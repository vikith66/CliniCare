package com.example.hospitalappointment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private ImageView btnUserProfile, btnAppointment, btnThemeToggle;
    private Button btnExit;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private RecyclerView rvTopDoctors;
    private TopDoctorAdapter topDoctorAdapter;
    private List<Doctor> topDoctorsList;
    private DatabaseReference doctorDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EdgeToEdge.enable(this);

        // Request notification permission
        NotificationManager.requestNotificationPermission(this);

        // --- Greeting User ---
        tvGreeting = findViewById(R.id.tvGreeting);
        btnUserProfile = findViewById(R.id.btnUserProfile);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnExit = findViewById(R.id.btnExit);
        mAuth = FirebaseAuth.getInstance();
        greetUser();
        
        // --- Theme Toggle Button Click Listener ---
        btnThemeToggle.setOnClickListener(v -> toggleTheme());
        
        // --- Exit Button Click Listener ---
        btnExit.setOnClickListener(v -> showExitConfirmationDialog());
        
        // --- User Profile Button Click Listener ---
        btnUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
        
        // --- Appointment History Button Click Listener ---
        btnAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AppointmentHistoryActivity.class);
            startActivity(intent);
        });

        // --- Search View ---
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search for doctor");
        
        // Set text color programmatically
        int searchTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        if (searchTextId != 0) {
            TextView searchText = searchView.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchDoctors(query);
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Show all top doctors when search is cleared
                    fetchTopRatedDoctors();
                } else {
                    searchDoctors(newText);
                }
                return true;
            }
        });

        // --- Top Doctors RecyclerView ---
        rvTopDoctors = findViewById(R.id.rvTopDoctors);
        rvTopDoctors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        topDoctorsList = new ArrayList<>();
        topDoctorAdapter = new TopDoctorAdapter(this, topDoctorsList);
        rvTopDoctors.setAdapter(topDoctorAdapter);

        fetchTopRatedDoctors();
        
        // Hide keyboard when tapping outside search view
        findViewById(R.id.main).setOnClickListener(v -> hideKeyboard());
        
        // Category Card Click Listeners
        findViewById(R.id.card1).setOnClickListener(v -> openCategory("Dentist"));
        findViewById(R.id.card2).setOnClickListener(v -> openCategory("Cardiologist"));
        findViewById(R.id.card3).setOnClickListener(v -> openCategory("Neurologist"));
        findViewById(R.id.card4).setOnClickListener(v -> openCategory("Dermatologist"));
        findViewById(R.id.card5).setOnClickListener(v -> openCategory("Pediatrician"));
        findViewById(R.id.card6).setOnClickListener(v -> openCategory("Orthopedic"));
        findViewById(R.id.card7).setOnClickListener(v -> openCategory("Eye Specialist"));
        findViewById(R.id.card8).setOnClickListener(v -> openCategory("Psychiatrist"));

    }
    private void openCategory(String category) {
        Intent intent = new Intent(HomeActivity.this, CategoryDoctorsActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    // --- Method to greet user by name ---
    private void greetUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            tvGreeting.setText("Hi " + name);
                        } else {
                            tvGreeting.setText("Hello!");
                        }
                    } else {
                        tvGreeting.setText("Hello!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvGreeting.setText("Hello!");
                }
            });
        } else {
            tvGreeting.setText("Hello!");
        }
    }

    // --- Fetch Top Rated Doctors from Secondary Firebase ---
    private void fetchTopRatedDoctors() {
        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("doctorApp");
        } catch (IllegalStateException e) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setDatabaseUrl("https://doctordetails-712c2-default-rtdb.firebaseio.com/")
                    .setApiKey("AIzaSyB6m5C02wyjd9KELdmPfrd2WH2bx8TesBE")
                    .setApplicationId("1:446332953537:android:01dbd5ff6420f19088c540")
                    .build();
            secondaryApp = FirebaseApp.initializeApp(this, options, "doctorApp");
        }

        doctorDbRef = FirebaseDatabase.getInstance(secondaryApp).getReference("DoctorDetails");

        // Fetch top 5 doctors by rating
        doctorDbRef.orderByChild("rating").limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        topDoctorsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Doctor doctor = ds.getValue(Doctor.class);
                            if (doctor != null) {
                                topDoctorsList.add(0, doctor); // Add at beginning to show highest rating first
                            }
                        }
                        topDoctorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Failed to load doctors: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- Search Doctors Method ---
    private void searchDoctors(String query) {
        if (query.trim().isEmpty()) {
            fetchTopRatedDoctors();
            return;
        }

        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("doctorApp");
        } catch (IllegalStateException e) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setDatabaseUrl("https://doctordetails-712c2-default-rtdb.firebaseio.com/")
                    .setApiKey("AIzaSyB6m5C02wyjd9KELdmPfrd2WH2bx8TesBE")
                    .setApplicationId("1:446332953537:android:01dbd5ff6420f19088c540")
                    .build();
            secondaryApp = FirebaseApp.initializeApp(this, options, "doctorApp");
        }

        doctorDbRef = FirebaseDatabase.getInstance(secondaryApp).getReference("DoctorDetails");

        // Search in doctor names and categories
        doctorDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topDoctorsList.clear();
                String searchQuery = query.toLowerCase().trim();
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Doctor doctor = ds.getValue(Doctor.class);
                    if (doctor != null) {
                        String doctorName = doctor.getName().toLowerCase();
                        String doctorCategory = doctor.getCategory().toLowerCase();
                        
                        // Check if search query matches name or category
                        if (doctorName.contains(searchQuery) || doctorCategory.contains(searchQuery)) {
                            topDoctorsList.add(doctor);
                        }
                    }
                }
                
                // Sort by rating (highest first)
                topDoctorsList.sort((d1, d2) -> Double.compare(d2.getRating(), d1.getRating()));
                
                topDoctorAdapter.notifyDataSetChanged();
                
                if (topDoctorsList.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No doctors found for: " + query, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Search failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Hide Keyboard Method ---
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus() != null ? getCurrentFocus().getWindowToken() : null, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NotificationManager.handlePermissionResult(requestCode, permissions, grantResults);
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🚪 Exit App");
        builder.setMessage("Are you sure you want to exit the application?\n\n" +
                "⚠️ IMPORTANT NOTICE:\n" +
                "• You will be logged out\n" +
                "• Any unsaved data will be lost\n" +
                "• You can restart the app anytime");
        
        builder.setPositiveButton("EXIT", (dialog, which) -> {
            // Sign out from Firebase
            if (mAuth.getCurrentUser() != null) {
                mAuth.signOut();
            }
            
            // Finish all activities and exit
            finishAffinity();
            System.exit(0);
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Make the positive button red to indicate danger
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });
    }

    private void toggleTheme() {
        int currentTheme = ThemeManager.getThemeMode(this);
        int newTheme;
        
        if (currentTheme == ThemeManager.THEME_LIGHT) {
            newTheme = ThemeManager.THEME_DARK;
        } else {
            newTheme = ThemeManager.THEME_LIGHT;
        }
        
        ThemeManager.setThemeMode(this, newTheme);
        
        // Restart activity to apply new theme
        recreate();
        
        // Show toast message
        String themeName = newTheme == ThemeManager.THEME_DARK ? "Dark" : "Light";
        Toast.makeText(this, "Switched to " + themeName + " theme", Toast.LENGTH_SHORT).show();
    }
}
