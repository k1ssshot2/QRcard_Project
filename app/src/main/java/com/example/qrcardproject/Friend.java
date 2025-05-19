package com.example.qrcardproject;

public class Friend {
    private String name;
    private String email;
    private String phone;
    private String department;
    private String position;
    private String kakaoId;
    private String instagramId;

    // 생성자
    public Friend(String name, String email, String phone, String department, String position, String kakaoId, String instagramId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.kakaoId = kakaoId;
        this.instagramId = instagramId;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public String getInstagramId() {
        return instagramId;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void setInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }
}


