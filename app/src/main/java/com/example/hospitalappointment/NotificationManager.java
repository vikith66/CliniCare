package com.example.hospitalappointment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationManager {
    
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    
    /**
     * Check if notification permission is granted
     */
    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for older versions
    }
    
    /**
     * Request notification permission from user
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationPermissionGranted(activity)) {
                ActivityCompat.requestPermissions(activity, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    /**
     * Show appointment confirmation notification
     */
    public static void showAppointmentConfirmation(Context context, String doctorName, 
                                                   String doctorCategory, String date, 
                                                   String time, String price, String paymentMethod) {
        if (isNotificationPermissionGranted(context)) {
            AppointmentNotificationService.showConfirmationNotification(
                    context, doctorName, doctorCategory, date, time, price, paymentMethod);
        }
    }
    
    /**
     * Show appointment cancellation notification
     */
    public static void showAppointmentCancellation(Context context, String doctorName, 
                                                   String doctorCategory, String date, String time) {
        if (isNotificationPermissionGranted(context)) {
            AppointmentNotificationService.showCancellationNotification(
                    context, doctorName, doctorCategory, date, time);
        }
    }
    
    /**
     * Show appointment reminder notification
     */
    public static void showAppointmentReminder(Context context, String doctorName, 
                                               String doctorCategory, String date, String time) {
        if (isNotificationPermissionGranted(context)) {
            AppointmentNotificationService.showReminderNotification(
                    context, doctorName, doctorCategory, date, time);
        }
    }
    
    /**
     * Handle permission request result
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}
