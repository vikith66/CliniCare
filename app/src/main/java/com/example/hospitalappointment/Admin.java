package com.example.hospitalappointment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Admin extends AppCompatActivity {

    private Button btnAddDoctor, btnManageList, btnViewAppointments, btnLogout;
    private TextView tvDoctorCount;

    private DatabaseReference doctorDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnAddDoctor = findViewById(R.id.buttonAddDoctor);
        btnManageList = findViewById(R.id.buttonManageList);
        btnViewAppointments = findViewById(R.id.buttonViewAppointments);
        btnLogout = findViewById(R.id.buttonLogout);
        tvDoctorCount = findViewById(R.id.tvDoctorCount);

        // ----- Initialize secondary Firebase project -----
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://doctordetails-712c2-default-rtdb.firebaseio.com")
                .setApiKey("AIzaSyB6m5C02wyjd9KELdmPfrd2WH2bx8TesBE")
                .setApplicationId("1:446332953537:android:01dbd5ff6420f19088c540")
                .build();

        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("doctorApp");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.initializeApp(getApplicationContext(), options, "doctorApp");
        }

        doctorDbRef = FirebaseDatabase.getInstance(secondaryApp).getReference("DoctorDetails");

        // Fetch and display number of doctors
        doctorDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvDoctorCount.setText(count + "\nTotal Doctors");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvDoctorCount.setText("Error");
            }
        });

        // Navigate to AddDoctorActivity
        btnAddDoctor.setOnClickListener(view -> {
            Intent intent = new Intent(Admin.this, AddDoctorActivity.class);
            startActivity(intent);
        });

        // Navigate to ManageDoctorsActivity
        btnManageList.setOnClickListener(view -> {
            Intent intent = new Intent(Admin.this, ManageDoctors.class);
            startActivity(intent);
        });

        // Navigate to AdminAppointmentsActivity
        btnViewAppointments.setOnClickListener(view -> {
            Intent intent = new Intent(Admin.this, AdminAppointmentsActivity.class);
            startActivity(intent);
        });

        // Navigate to LoginSignupActivity on Logout
        btnLogout.setOnClickListener(view -> {
            Intent intent = new Intent(Admin.this, LoginSignupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
