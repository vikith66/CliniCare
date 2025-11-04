package com.example.hospitalappointment;

public class Appointment {
    private String id;        // appointmentId
    private String userId;    // UID of user
    private String doctorId;  // Doctor ID
    private String doctorName;
    private String doctorCategory;
    private String date;
    private String time;

    public Appointment() {
        // empty constructor needed for Firebase
    }

    public Appointment(String id, String userId, String doctorId,
                       String doctorName, String doctorCategory,
                       String date, String time) {
        this.id = id;
        this.userId = userId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorCategory = doctorCategory;
        this.date = date;
        this.time = time;
    }

    // getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorCategory() { return doctorCategory; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    // setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDoctorCategory(String doctorCategory) { this.doctorCategory = doctorCategory; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
}
