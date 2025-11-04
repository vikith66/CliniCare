package com.example.hospitalappointment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for scheduling appointment reminders
 */
public class AppointmentReminderScheduler {
    
    private static final int REMINDER_REQUEST_CODE = 2001;
    
    /**
     * Schedule appointment reminder notification
     * @param context Application context
     * @param doctorName Doctor's name
     * @param doctorCategory Doctor's specialty
     * @param appointmentDate Appointment date (format: "dd MMM")
     * @param appointmentTime Appointment time (format: "HH:mm")
     * @param reminderMinutes Minutes before appointment to show reminder (default: 30)
     */
    public static void scheduleAppointmentReminder(Context context, String doctorName, 
                                                   String doctorCategory, String appointmentDate, 
                                                   String appointmentTime, int reminderMinutes) {
        
        try {
            // Parse appointment date and time
            Calendar appointmentCalendar = parseAppointmentDateTime(appointmentDate, appointmentTime);
            
            // Calculate reminder time
            Calendar reminderCalendar = (Calendar) appointmentCalendar.clone();
            reminderCalendar.add(Calendar.MINUTE, -reminderMinutes);
            
            // Don't schedule if reminder time has already passed
            if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                return;
            }
            
            // Create intent for reminder
            Intent reminderIntent = new Intent(context, AppointmentReminderReceiver.class);
            reminderIntent.putExtra("doctorName", doctorName);
            reminderIntent.putExtra("doctorCategory", doctorCategory);
            reminderIntent.putExtra("appointmentDate", appointmentDate);
            reminderIntent.putExtra("appointmentTime", appointmentTime);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    REMINDER_REQUEST_CODE + doctorName.hashCode(), 
                    reminderIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule the alarm
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.getTimeInMillis(),
                        pendingIntent
                );
            }
            
        } catch (Exception e) {
            // Log error but don't crash the app
            e.printStackTrace();
        }
    }
    
    /**
     * Cancel appointment reminder
     */
    public static void cancelAppointmentReminder(Context context, String doctorName) {
        Intent reminderIntent = new Intent(context, AppointmentReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                REMINDER_REQUEST_CODE + doctorName.hashCode(), 
                reminderIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    /**
     * Parse appointment date and time string into Calendar object
     */
    private static Calendar parseAppointmentDateTime(String date, String time) {
        Calendar calendar = Calendar.getInstance();
        
        try {
            // Parse date (format: "dd MMM")
            String[] dateParts = date.split(" ");
            int day = Integer.parseInt(dateParts[0]);
            String monthStr = dateParts[1];
            
            // Convert month string to number
            int month = getMonthNumber(monthStr);
            
            // Parse time (format: "HH:mm")
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Set calendar to appointment date/time
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // If the appointment is in the past, assume it's next year
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.YEAR, 1);
            }
            
        } catch (Exception e) {
            // If parsing fails, set to current time + 1 hour as fallback
            calendar.setTimeInMillis(System.currentTimeMillis() + 3600000);
        }
        
        return calendar;
    }
    
    /**
     * Convert month abbreviation to month number
     */
    private static int getMonthNumber(String monthStr) {
        switch (monthStr.toLowerCase()) {
            case "jan": return Calendar.JANUARY;
            case "feb": return Calendar.FEBRUARY;
            case "mar": return Calendar.MARCH;
            case "apr": return Calendar.APRIL;
            case "may": return Calendar.MAY;
            case "jun": return Calendar.JUNE;
            case "jul": return Calendar.JULY;
            case "aug": return Calendar.AUGUST;
            case "sep": return Calendar.SEPTEMBER;
            case "oct": return Calendar.OCTOBER;
            case "nov": return Calendar.NOVEMBER;
            case "dec": return Calendar.DECEMBER;
            default: return Calendar.JANUARY;
        }
    }
}
