package com.example.hospitalappointment;

public class AdminAppointmentData {
    private String appointmentId;
    private String userId;
    private String userEmail;
    private String doctorName;
    private String doctorId;
    private String doctorCategory;
    private String date;
    private String time;
    private String status;
    private String doctorImage;
    private String doctorPrice;
    private double doctorRating;
    private String paymentMethod;

    public AdminAppointmentData() {
        // Default constructor required for Firebase
    }

    public AdminAppointmentData(String appointmentId, String userId, String userEmail, 
                               String doctorName, String doctorId, String doctorCategory, 
                               String date, String time, String status, String doctorImage, 
                               String doctorPrice, double doctorRating, String paymentMethod) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.doctorCategory = doctorCategory;
        this.date = date;
        this.time = time;
        this.status = status;
        this.doctorImage = doctorImage;
        this.doctorPrice = doctorPrice;
        this.doctorRating = doctorRating;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorCategory() {
        return doctorCategory;
    }

    public void setDoctorCategory(String doctorCategory) {
        this.doctorCategory = doctorCategory;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }

    public String getDoctorPrice() {
        return doctorPrice;
    }

    public void setDoctorPrice(String doctorPrice) {
        this.doctorPrice = doctorPrice;
    }

    public double getDoctorRating() {
        return doctorRating;
    }

    public void setDoctorRating(double doctorRating) {
        this.doctorRating = doctorRating;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
