package com.example.hospitalappointment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopDoctorAdapter extends RecyclerView.Adapter<TopDoctorAdapter.TopDoctorViewHolder> {

    private final Context context;
    private final List<Doctor> doctorList;

    public TopDoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public TopDoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_doctor, parent, false);
        return new TopDoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopDoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        // Split name for two lines if needed
        String[] parts = doctor.getName().split(" ", 2);
        if (parts.length == 2) {
            holder.tvDoctorName.setText(parts[0] + "\n" + parts[1]);
        } else {
            holder.tvDoctorName.setText(doctor.getName());
        }

        // Set category
        holder.tvDoctorCategory.setText(doctor.getCategory());

        // Set rating
        holder.rbDoctorRating.setNumStars(5);
        holder.rbDoctorRating.setStepSize(0.5f);
        holder.rbDoctorRating.setRating((float) doctor.getRating()); // use double -> float

        // Set image
        if (doctor.getImageBase64() != null && !doctor.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(doctor.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivDoctorImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivDoctorImage.setImageResource(R.drawable.ic_doctor_placeholder);
            }
        } else {
            holder.ivDoctorImage.setImageResource(R.drawable.ic_doctor_placeholder);
        }

        // Set click listener for the entire card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookAppointment.class);
            intent.putExtra("doctorId", doctor.getId());
            intent.putExtra("doctorName", doctor.getName());
            intent.putExtra("doctorCategory", doctor.getCategory());
            intent.putExtra("doctorPrice", doctor.getFees());
            intent.putExtra("doctorAvailability", doctor.getAvailability());
            intent.putExtra("doctorRating", doctor.getRating());
            intent.putExtra("doctorImage", doctor.getImageBase64());
            intent.putExtra("doctorAbout", doctor.getAbout());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    // --- ViewHolder ---
    static class TopDoctorViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView ivDoctorImage;
        final TextView tvDoctorName, tvDoctorCategory;
        final RatingBar rbDoctorRating;

        public TopDoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoctorImage = itemView.findViewById(R.id.ivDoctorImage);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCategory = itemView.findViewById(R.id.tvDoctorCategory);
            rbDoctorRating = itemView.findViewById(R.id.rbDoctorRating);
        }
    }
}
