package com.example.qrcardproject;

import java.io.Serializable;

public class Friend implements Serializable, FriendListItem {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String position;
    private boolean isFavorite;
    private boolean showAlert;

    public Friend() {}

    public Friend(String name, String email, String phone, String department, String position, boolean isFavorite, boolean showAlert) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.isFavorite = isFavorite;
        this.showAlert = showAlert;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public boolean isFavorite() { return isFavorite; }
    public boolean shouldShowAlert() { return showAlert; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public void setShowAlert(boolean showAlert) { this.showAlert = showAlert; }
}



