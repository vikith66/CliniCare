package com.example.hospitalappointment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver for handling appointment reminder notifications
 */
public class AppointmentReminderReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String doctorCategory = intent.getStringExtra("doctorCategory");
        String appointmentDate = intent.getStringExtra("appointmentDate");
        String appointmentTime = intent.getStringExtra("appointmentTime");
        
        // Show reminder notification
        NotificationManager.showAppointmentReminder(
                context,
                doctorName,
                doctorCategory,
                appointmentDate,
                appointmentTime
        );
    }
}
