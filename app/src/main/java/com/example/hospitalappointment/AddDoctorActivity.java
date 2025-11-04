package com.example.hospitalappointment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddDoctorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private EditText etName, etRatings, etFees, etAbout;
    private Spinner spCategory;
    private RadioGroup rgAvailability;
    private Button btnSubmit, btnPickImage;
    private ImageView ivPreview;

    private Uri imageUri;
    private DatabaseReference doctorDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);
        EdgeToEdge.enable(this);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

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

        // ----- Initialize Views -----
        etName = findViewById(R.id.etDoctorName);
        spCategory = findViewById(R.id.spDoctorCategory);
        etRatings = findViewById(R.id.etDoctorRatings);
        etFees = findViewById(R.id.etDoctorPrice);
        etAbout = findViewById(R.id.etDoctorAbout);
        rgAvailability = findViewById(R.id.rgAvailability);
        btnSubmit = findViewById(R.id.btnAddDoctorSubmit);
        btnPickImage = findViewById(R.id.btnimage);
        ivPreview = findViewById(R.id.ivDoctorPreview);

        setupCategorySpinner();
        btnPickImage.setOnClickListener(v -> pickImage());
        btnSubmit.setOnClickListener(v -> uploadDoctorData());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Select Category", "Dentist", "Cardiologist", "Neurologist",
                "Dermatologist", "Pediatrician", "Orthopedic", "Eye Specialist", "Psychiatrist"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories) {

            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable first item
            }

            @NonNull
            @Override
            public TextView getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextSize(tv.getTextSize() / getResources().getDisplayMetrics().density + 1);
                tv.setTextColor(0xFF595959);
                return tv;
            }

            @Override
            public TextView getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextSize(tv.getTextSize() / getResources().getDisplayMetrics().density + 1);
                tv.setTextColor(0xFFFFFFFF);
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? 0xFF595959 : 0xFF595959);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Doctor Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
        }
    }

    private void uploadDoctorData() {
        String name = etName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String ratingsStr = etRatings.getText().toString().trim();
        String feesStr = etFees.getText().toString().trim();
        String about = etAbout.getText().toString().trim();

        // ----- Validations -----
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ratingsStr) ||
                TextUtils.isEmpty(feesStr) || TextUtils.isEmpty(about) || imageUri == null) {
            Toast.makeText(this, "Fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!name.matches("[a-zA-Z\\s]+")) {
            Toast.makeText(this, "Doctor name should not contain numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Select Category")) {
            Toast.makeText(this, "Please select a specialist", Toast.LENGTH_SHORT).show();
            return;
        }

        // ----- Parse Ratings & Fees -----
        double ratings;
        try {
            ratings = Double.parseDouble(ratingsStr);
            if (ratings < 0 || ratings > 5) {
                Toast.makeText(this, "Ratings must be between 0 and 5", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid ratings value", Toast.LENGTH_SHORT).show();
            return;
        }

        String fees = feesStr; // Keep as string for your Doctor model

        String availability = (rgAvailability.getCheckedRadioButtonId() == R.id.rbAvailable)
                ? "Available" : "Not Available";

        String doctorId = doctorDbRef.push().getKey(); // Generate unique ID

        // ----- Convert image to Base64 -----
        String imageBase64 = "";
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        // ----- Create Doctor object (ratings as double) -----
        Doctor doctor = new Doctor(doctorId, name, category, ratings, fees, about, availability, imageBase64);

        // ----- Save to Firebase -----
        doctorDbRef.child(doctorId).setValue(doctor)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Doctor added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add doctor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
