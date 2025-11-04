package com.example.hospitalappointment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private Context context;
    private List<Doctor> doctorList;
    private DatabaseReference doctorDbRef;
    private boolean isAdmin;

    public DoctorAdapter(Context context, List<Doctor> doctorList, DatabaseReference doctorDbRef, boolean isAdmin) {
        this.context = context;
        this.doctorList = doctorList;
        this.doctorDbRef = doctorDbRef;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        holder.tvDoctorName.setText(doctor.getName());
        holder.tvDoctorCategory.setText(doctor.getCategory());
        holder.tvDoctorAvailability.setText(doctor.getAvailability());

        // availability text color
        holder.tvDoctorAvailability.setTextColor(
                doctor.isAvailable() ?
                        context.getResources().getColor(android.R.color.holo_green_dark) :
                        context.getResources().getColor(android.R.color.holo_red_dark)
        );

        // doctor image
        if (doctor.getImageBase64() != null && !doctor.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(doctor.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.ivDoctorImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
            }
        } else {
            holder.ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
        }

        // remove doctor (only for admin)
        holder.btnRemoveDoctor.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnRemoveDoctor.setOnClickListener(v -> {
            doctorDbRef.child(doctor.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        doctorList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, doctorList.size());
                        Toast.makeText(context, "Doctor removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        // toggle availability (only for admin)
        if (isAdmin) {
            holder.tvDoctorAvailability.setOnClickListener(v -> {
                    String newStatus = doctor.isAvailable() ? Doctor.NOT_AVAILABLE : Doctor.AVAILABLE;
                doctorDbRef.child(doctor.getId()).child("availability").setValue(newStatus)
                        .addOnSuccessListener(aVoid -> {
                            doctor.setAvailability(newStatus);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Availability updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show());
            });
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDoctorImage;
        TextView tvDoctorName, tvDoctorCategory, tvDoctorAvailability;
        Button btnRemoveDoctor;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoctorImage = itemView.findViewById(R.id.ivDoctorImage);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCategory = itemView.findViewById(R.id.tvDoctorCategory);
            tvDoctorAvailability = itemView.findViewById(R.id.tvDoctorAvailability);
            btnRemoveDoctor = itemView.findViewById(R.id.btnRemoveDoctor);
        }
    }
}
