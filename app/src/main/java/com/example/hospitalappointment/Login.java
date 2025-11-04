package com.example.hospitalappointment;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView backArrow;
    private TextView tvSignupHint, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        // Link UI components
        etEmail         = findViewById(R.id.editTextTextEmailAddress);
        etPassword      = findViewById(R.id.editTextTextPassword);
        btnLogin        = findViewById(R.id.button);
        backArrow       = findViewById(R.id.imageView4);
        tvSignupHint    = findViewById(R.id.textView6);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Login button click
        btnLogin.setOnClickListener(v -> validateLogin());

        // Back arrow click
        backArrow.setOnClickListener(v -> finish());

        // Pre-fill email if coming from forgot password
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra("email")) {
            String email = receivedIntent.getStringExtra("email");
            if (email != null && !email.isEmpty()) {
                etEmail.setText(email);
            }
        }

        // Navigate to Signup screen
        tvSignupHint.setOnClickListener(v -> {
            Intent signupIntent = new Intent(Login.this, Signup.class);
            startActivity(signupIntent);
        });

        // Forgot Password click
        tvForgotPassword.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(Login.this, ForgotPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    private void validateLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // First try Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Auth login successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) return;

                        // --- Admin check ---
                        if (email.equalsIgnoreCase("admin@hospital.com") && password.equals("admin123")) {
                            Toast.makeText(Login.this, "Hello Admin", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Admin.class));
                            finish();
                            return;
                        }

                        // --- Normal user ---
                        String uid = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid);

                        userRef.get().addOnCompleteListener(snapshotTask -> {
                            String name;
                        if (snapshotTask.isSuccessful() && snapshotTask.getResult().exists()) {
                            name = snapshotTask.getResult().child("name").getValue(String.class);
                            if (name == null || name.isEmpty()) {
                                name = user.getEmail(); // fallback
                            }
                        } else {
                            name = user.getEmail(); // fallback
                        }


                        Toast.makeText(Login.this, "Hello " + name, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, HomeActivity.class));
                        finish();
                        });

                    } else {
                        // Firebase Auth failed, try database login
                        checkDatabaseLogin(email, password);
                    }
                });
    }

    private void checkDatabaseLogin(String email, String password) {
        // Query database for user with matching email and password
        Query query = FirebaseDatabase.getInstance()
                .getReference("users")
                .orderByChild("email")
                .equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User found, check password
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(password)) {
                            // Password matches, login successful
                            String name = userSnapshot.child("name").getValue(String.class);
                            String userId = userSnapshot.getKey();
                            if (name == null || name.isEmpty()) {
                                name = email;
                            }


                            Toast.makeText(Login.this, "Hello " + name, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, HomeActivity.class));
                            finish();
                            return;
                        }
                    }
                }
                // No user found or password doesn't match
                Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
