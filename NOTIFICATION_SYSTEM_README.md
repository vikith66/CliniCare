# Appointment Notification System

This document explains the comprehensive notification system implemented for the CliniCare Android app, including appointment confirmations, cancellations, and reminders.

## Features

### 1. Appointment Confirmation Notifications
- **Trigger**: When an appointment is successfully booked
- **Content**: Doctor name, specialty, date, time, price, and payment method
- **Visual**: Green gradient background with checkmark icon
- **Actions**: "View Appointments" button

### 2. Appointment Cancellation Notifications
- **Trigger**: When an appointment is cancelled by the user
- **Content**: Doctor name, specialty, date, and time
- **Visual**: Red gradient background with X icon
- **Actions**: "Book New Appointment" button

### 3. Appointment Reminder Notifications
- **Trigger**: 30 minutes before scheduled appointment
- **Content**: Doctor name, specialty, date, and time
- **Visual**: Blue gradient background with clock icon
- **Actions**: Tap to view appointment details

## Implementation Details

### Core Components

#### 1. AppointmentNotificationService.java
- **Purpose**: Handles all notification display logic
- **Features**:
  - Creates notification channels for different notification types
  - Displays rich notifications with custom layouts
  - Handles notification actions and intents
  - Provides static methods for easy integration

#### 2. NotificationManager.java
- **Purpose**: Provides a clean interface for notification management
- **Features**:
  - Permission handling for Android 13+
  - Static methods for showing different notification types
  - Permission request handling

#### 3. AppointmentReminderScheduler.java
- **Purpose**: Schedules appointment reminder notifications
- **Features**:
  - Parses appointment date/time strings
  - Schedules exact alarms for reminders
  - Handles reminder cancellation
  - Fallback handling for parsing errors

#### 4. AppointmentReminderReceiver.java
- **Purpose**: BroadcastReceiver for handling scheduled reminders
- **Features**:
  - Receives alarm broadcasts
  - Triggers reminder notifications
  - Extracts appointment data from intents

### Notification Channels

The system creates three distinct notification channels:

1. **Appointment Confirmations** (`appointment_confirmation`)
   - High importance
   - Green light color
   - Custom vibration pattern

2. **Appointment Cancellations** (`appointment_cancellation`)
   - High importance
   - Red light color
   - Custom vibration pattern

3. **Appointment Reminders** (`appointment_reminder`)
   - Default importance
   - Blue light color
   - Custom vibration pattern

### Visual Design

#### Notification Layouts
- **Custom layouts**: `notification_appointment_confirmation.xml` and `notification_appointment_cancellation.xml`
- **Backgrounds**: Gradient backgrounds with rounded corners
- **Icons**: Custom vector drawable icons for different notification types
- **Typography**: Clear hierarchy with bold titles and readable content

#### Color Scheme
- **Confirmation**: Green gradient (#4CAF50 to #2E7D32)
- **Cancellation**: Red gradient (#F44336 to #C62828)
- **Reminder**: Blue gradient (system default)

### Integration Points

#### 1. PaymentActivity.java
```java
// After successful appointment booking
NotificationManager.showAppointmentConfirmation(
    context, doctorName, doctorCategory, date, time, price, paymentMethod
);

// Schedule reminder
AppointmentReminderScheduler.scheduleAppointmentReminder(
    context, doctorName, doctorCategory, date, time, 30
);
```

#### 2. AppointmentHistoryActivity.java
```java
// After successful appointment cancellation
NotificationManager.showAppointmentCancellation(
    context, doctorName, doctorCategory, date, time
);

// Cancel reminder
AppointmentReminderScheduler.cancelAppointmentReminder(context, doctorName);
```

#### 3. HomeActivity.java
```java
// Request notification permission on app start
NotificationManager.requestNotificationPermission(this);
```

### Permissions Required

The following permissions are added to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

### Usage Examples

#### Showing a Confirmation Notification
```java
NotificationManager.showAppointmentConfirmation(
    context,
    "Dr. Smith",
    "Cardiology",
    "15 Dec",
    "14:30",
    "500",
    "Credit Card"
);
```

#### Showing a Cancellation Notification
```java
NotificationManager.showAppointmentCancellation(
    context,
    "Dr. Smith",
    "Cardiology",
    "15 Dec",
    "14:30"
);
```

#### Scheduling a Reminder
```java
AppointmentReminderScheduler.scheduleAppointmentReminder(
    context,
    "Dr. Smith",
    "Cardiology",
    "15 Dec",
    "14:30",
    30 // 30 minutes before
);
```

### Error Handling

- **Permission Denied**: Notifications are silently skipped if permission is not granted
- **Parsing Errors**: Date/time parsing errors fall back to current time + 1 hour
- **Service Errors**: Notification service errors are logged but don't crash the app
- **Alarm Scheduling**: Failed alarm scheduling is handled gracefully

### Testing

To test the notification system:

1. **Confirmation**: Book an appointment and verify notification appears
2. **Cancellation**: Cancel an appointment and verify notification appears
3. **Reminder**: Book an appointment for a future time and verify reminder is scheduled
4. **Permissions**: Test on Android 13+ devices to verify permission requests

### Future Enhancements

Potential improvements for the notification system:

1. **Rich Media**: Add doctor profile images to notifications
2. **Multiple Reminders**: Schedule multiple reminders (1 day, 1 hour, 30 minutes before)
3. **Customization**: Allow users to customize reminder timing
4. **Push Notifications**: Integrate with Firebase Cloud Messaging for server-side notifications
5. **Notification History**: Store notification history for user reference

## Troubleshooting

### Common Issues

1. **Notifications not appearing**: Check if notification permission is granted
2. **Reminders not working**: Verify alarm permissions are granted
3. **Wrong date/time**: Check date format parsing in AppointmentReminderScheduler
4. **Service not starting**: Verify service is properly declared in manifest

### Debug Tips

- Check notification channels in device settings
- Use `adb shell dumpsys notification` to debug notification issues
- Monitor logcat for notification service logs
- Test on different Android versions for compatibility
