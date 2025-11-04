package com.example.hospitalappointment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.AppointmentViewHolder> {

    private List<AdminAppointmentData> appointments;
    private AdminAppointmentsActivity activity;

    public AdminAppointmentAdapter(AdminAppointmentsActivity activity, List<AdminAppointmentData> appointments) {
        this.activity = activity;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AdminAppointmentData appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAppointmentId, tvPatientEmail, tvUserId;
        private TextView tvDoctorName, tvDoctorCategory;
        private TextView tvDate, tvTime, tvStatus;
        private TextView tvPaymentMethod, tvDoctorPrice;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppointmentId = itemView.findViewById(R.id.tvAppointmentId);
            tvPatientEmail = itemView.findViewById(R.id.tvPatientEmail);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCategory = itemView.findViewById(R.id.tvDoctorCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvDoctorPrice = itemView.findViewById(R.id.tvDoctorPrice);
        }

        public void bind(AdminAppointmentData appointment) {
            tvAppointmentId.setText("Appointment #" + (appointment.getAppointmentId() != null ? appointment.getAppointmentId().substring(0, Math.min(8, appointment.getAppointmentId().length())) : "N/A"));
            tvPatientEmail.setText(appointment.getUserEmail() != null ? appointment.getUserEmail() : "N/A");
            tvUserId.setText("User ID: " + (appointment.getUserId() != null ? appointment.getUserId() : "N/A"));
            tvDoctorName.setText(appointment.getDoctorName() != null ? appointment.getDoctorName() : "N/A");
            tvDoctorCategory.setText(appointment.getDoctorCategory() != null ? appointment.getDoctorCategory() : "N/A");
            tvDate.setText(appointment.getDate() != null ? appointment.getDate() : "N/A");
            tvTime.setText(appointment.getTime() != null ? appointment.getTime() : "N/A");
            tvPaymentMethod.setText("Payment: " + (appointment.getPaymentMethod() != null ? appointment.getPaymentMethod() : "N/A"));
            tvDoctorPrice.setText("Fees: ₹" + (appointment.getDoctorPrice() != null ? appointment.getDoctorPrice() : "N/A"));

            // Set status with appropriate styling
            String status = appointment.getStatus() != null ? appointment.getStatus() : "unknown";
            tvStatus.setText(status.toUpperCase());
            
            // Set status background color
            switch (status.toLowerCase()) {
                case "upcoming":
                    tvStatus.setBackgroundResource(R.drawable.status_upcoming_background);
                    break;
                case "completed":
                    tvStatus.setBackgroundResource(R.drawable.status_completed_background);
                    break;
                case "cancelled":
                    tvStatus.setBackgroundResource(R.drawable.status_cancelled_background);
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.status_upcoming_background);
                    break;
            }
        }
    }
}
