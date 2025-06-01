package com.example.qrcardproject;
import java.io.Serializable;

public class Friend implements Serializable, FriendListItem{
    private String id;           // Firestore 문서 ID
    private String name;
    private String email;

    private String phone;
    private String department;
    private String position;
    private boolean isFavorite;

    // Firestore에서 객체를 자동으로 매핑하려면 기본 생성자 필요
    public Friend() {}

    // 생성자 (id는 Firestore에서 나중에 설정)
    public Friend(String name, String email, String Phone,String department, String position, boolean isfavorite) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.isFavorite = isfavorite;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {return phone;}

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }




}



