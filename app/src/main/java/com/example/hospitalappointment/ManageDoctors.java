package com.example.hospitalappointment;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctors extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;
    private DatabaseReference doctorDbRef;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_doctors);
        EdgeToEdge.enable(this);

        rvDoctors = findViewById(R.id.rvDoctors);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // --- Initialize secondary Firebase app ---
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

        // Setup RecyclerView
        doctorList = new ArrayList<>();
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAdapter(this, doctorList, doctorDbRef, true); // admin = true
        rvDoctors.setAdapter(adapter);

        fetchDoctorsFromFirebase();
    }

    private void fetchDoctorsFromFirebase() {
        doctorDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Doctor doctor = ds.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.setId(ds.getKey());
                        doctorList.add(doctor);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDoctors.this,
                        "Failed to load doctors: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
