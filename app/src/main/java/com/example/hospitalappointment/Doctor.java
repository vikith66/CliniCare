package com.example.hospitalappointment;

public class Doctor {
    private String id;
    private String name;
    private String category;
    private double rating; // change from String to double
    private String fees;
    private String about;
    private String availability;
    private String imageBase64;
    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";

    // Helper method
    public boolean isAvailable() {
        return AVAILABLE.equalsIgnoreCase(this.availability);
    }

    public Doctor() { }

    public Doctor(String id, String name, String category, double rating, String fees,
                  String about, String availability, String imageBase64) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.rating = rating; // double
        this.fees = fees;
        this.about = about;
        this.availability = availability;
        this.imageBase64 = imageBase64;
    }

    public double getRating() { return rating; }   // <- note
    public void setRating(double rating) { this.rating = rating; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFees() { return fees; }
    public void setFees(String fees) { this.fees = fees; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
