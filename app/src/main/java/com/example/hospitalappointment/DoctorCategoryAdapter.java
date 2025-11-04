package com.example.hospitalappointment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorCategoryAdapter extends RecyclerView.Adapter<DoctorCategoryAdapter.DoctorViewHolder> {

    private final Context context;
    private final List<Doctor> doctorList;

    public DoctorCategoryAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_category_card, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        holder.tvDoctorName.setText(doctor.getName());
        holder.tvDoctorCategory.setText(doctor.getCategory());
        holder.tvDoctorRating.setText("⭐ " + doctor.getRating());
        holder.tvDoctorAvailability.setText(doctor.getAvailability());

        // image
        if (doctor.getImageBase64() != null && !doctor.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(doctor.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivDoctorImage.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                holder.ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
            }
        } else {
            holder.ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
        }

        // booking button → open BookAppointment
        holder.btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookAppointment.class);
            intent.putExtra("doctorId", doctor.getId());   // 🔑 must send ID
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

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDoctorImage;
        TextView tvDoctorName, tvDoctorCategory, tvDoctorRating, tvDoctorAvailability;
        Button btnBookAppointment;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoctorImage = itemView.findViewById(R.id.ivDoctorImage);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCategory = itemView.findViewById(R.id.tvDoctorCategory);
            tvDoctorRating = itemView.findViewById(R.id.tvDoctorRating);
            tvDoctorAvailability = itemView.findViewById(R.id.tvDoctorAvailability);
            btnBookAppointment = itemView.findViewById(R.id.btnBookAppointment);
        }
    }
}
