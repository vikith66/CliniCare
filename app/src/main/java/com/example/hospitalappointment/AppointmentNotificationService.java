package com.example.hospitalappointment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class AppointmentNotificationService extends Service {
    
    private static final String TAG = "AppointmentNotification";
    private static final String CHANNEL_ID_CONFIRMATION = "appointment_confirmation";
    private static final String CHANNEL_ID_CANCELLATION = "appointment_cancellation";
    private static final String CHANNEL_ID_REMINDER = "appointment_reminder";
    
    private static final int NOTIFICATION_ID_CONFIRMATION = 1001;
    private static final int NOTIFICATION_ID_CANCELLATION = 1002;
    private static final int NOTIFICATION_ID_REMINDER = 1003;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if ("show_confirmation".equals(action)) {
                showAppointmentConfirmationNotification(intent);
            } else if ("show_cancellation".equals(action)) {
                showAppointmentCancellationNotification(intent);
            } else if ("show_reminder".equals(action)) {
                showAppointmentReminderNotification(intent);
            }
        }
        return START_NOT_STICKY;
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            
            // Confirmation Channel
            NotificationChannel confirmationChannel = new NotificationChannel(
                    CHANNEL_ID_CONFIRMATION,
                    "Appointment Confirmations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            confirmationChannel.setDescription("Notifications for appointment confirmations");
            confirmationChannel.enableLights(true);
            confirmationChannel.setLightColor(Color.GREEN);
            confirmationChannel.enableVibration(true);
            confirmationChannel.setVibrationPattern(new long[]{0, 300, 200, 300});
            
            // Cancellation Channel
            NotificationChannel cancellationChannel = new NotificationChannel(
                    CHANNEL_ID_CANCELLATION,
                    "Appointment Cancellations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            cancellationChannel.setDescription("Notifications for appointment cancellations");
            cancellationChannel.enableLights(true);
            cancellationChannel.setLightColor(Color.RED);
            cancellationChannel.enableVibration(true);
            cancellationChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            
            // Reminder Channel
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID_REMINDER,
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Notifications for appointment reminders");
            reminderChannel.enableLights(true);
            reminderChannel.setLightColor(Color.BLUE);
            reminderChannel.enableVibration(true);
            reminderChannel.setVibrationPattern(new long[]{0, 200, 100, 200});
            
            notificationManager.createNotificationChannel(confirmationChannel);
            notificationManager.createNotificationChannel(cancellationChannel);
            notificationManager.createNotificationChannel(reminderChannel);
        }
    }
    
    private void showAppointmentConfirmationNotification(Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String doctorCategory = intent.getStringExtra("doctorCategory");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String price = intent.getStringExtra("price");
        String paymentMethod = intent.getStringExtra("paymentMethod");
        
        // Create intent for when notification is tapped
        Intent tapIntent = new Intent(this, AppointmentHistoryActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create custom notification layout
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_CONFIRMATION)
                .setSmallIcon(R.drawable.ic_notification_confirm)
                .setLargeIcon(getAppIcon())
                .setContentTitle("✅ Appointment Confirmed!")
                .setContentText("Your appointment with Dr. " + doctorName + " has been confirmed")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("✅ Appointment Confirmed!\n\n" +
                                "Doctor: Dr. " + doctorName + "\n" +
                                "Specialty: " + doctorCategory + "\n" +
                                "Date: " + date + "\n" +
                                "Time: " + time + "\n" +
                                "Amount: ₹" + price + "\n" +
                                "Payment: " + paymentMethod + "\n\n" +
                                "Please arrive 15 minutes before your scheduled time."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.GREEN)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Add action buttons
        Intent viewIntent = new Intent(this, AppointmentHistoryActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(
                this, 1, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        builder.addAction(R.drawable.ic_view_appointments, "View Appointments", viewPendingIntent);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID_CONFIRMATION, builder.build());
        
        Log.d(TAG, "Appointment confirmation notification sent");
    }
    
    private void showAppointmentCancellationNotification(Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String doctorCategory = intent.getStringExtra("doctorCategory");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        
        // Create intent for when notification is tapped
        Intent tapIntent = new Intent(this, AppointmentHistoryActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                this, 2, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_CANCELLATION)
                .setSmallIcon(R.drawable.ic_notification_cancel)
                .setLargeIcon(getAppIcon())
                .setContentTitle("❌ Appointment Cancelled")
                .setContentText("Your appointment with Dr. " + doctorName + " has been cancelled")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("❌ Appointment Cancelled\n\n" +
                                "Doctor: Dr. " + doctorName + "\n" +
                                "Specialty: " + doctorCategory + "\n" +
                                "Date: " + date + "\n" +
                                "Time: " + time + "\n\n" +
                                "Your appointment has been successfully cancelled. " +
                                "You can book a new appointment anytime."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.RED)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Add action buttons
        Intent bookIntent = new Intent(this, HomeActivity.class);
        PendingIntent bookPendingIntent = PendingIntent.getActivity(
                this, 3, bookIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        builder.addAction(R.drawable.ic_book_appointment, "Book New Appointment", bookPendingIntent);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID_CANCELLATION, builder.build());
        
        Log.d(TAG, "Appointment cancellation notification sent");
    }
    
    private void showAppointmentReminderNotification(Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String doctorCategory = intent.getStringExtra("doctorCategory");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        
        // Create intent for when notification is tapped
        Intent tapIntent = new Intent(this, AppointmentHistoryActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                this, 4, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_REMINDER)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setLargeIcon(getAppIcon())
                .setContentTitle("⏰ Appointment Reminder")
                .setContentText("You have an appointment with Dr. " + doctorName + " today")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("⏰ Appointment Reminder\n\n" +
                                "Doctor: Dr. " + doctorName + "\n" +
                                "Specialty: " + doctorCategory + "\n" +
                                "Date: " + date + "\n" +
                                "Time: " + time + "\n\n" +
                                "Please arrive 15 minutes before your scheduled time."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.BLUE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID_REMINDER + new Random().nextInt(1000), builder.build());
        
        Log.d(TAG, "Appointment reminder notification sent");
    }
    
    private Bitmap getAppIcon() {
        return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    }
    
    // Static methods to trigger notifications from other classes
    public static void showConfirmationNotification(Context context, String doctorName, 
                                                   String doctorCategory, String date, 
                                                   String time, String price, String paymentMethod) {
        Intent intent = new Intent(context, AppointmentNotificationService.class);
        intent.putExtra("action", "show_confirmation");
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("doctorCategory", doctorCategory);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("price", price);
        intent.putExtra("paymentMethod", paymentMethod);
        context.startService(intent);
    }
    
    public static void showCancellationNotification(Context context, String doctorName, 
                                                   String doctorCategory, String date, String time) {
        Intent intent = new Intent(context, AppointmentNotificationService.class);
        intent.putExtra("action", "show_cancellation");
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("doctorCategory", doctorCategory);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        context.startService(intent);
    }
    
    public static void showReminderNotification(Context context, String doctorName, 
                                               String doctorCategory, String date, String time) {
        Intent intent = new Intent(context, AppointmentNotificationService.class);
        intent.putExtra("action", "show_reminder");
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("doctorCategory", doctorCategory);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        context.startService(intent);
    }
}
