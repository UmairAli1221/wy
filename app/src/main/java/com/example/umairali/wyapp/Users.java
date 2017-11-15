package com.example.umairali.wyapp;

/**
 * Created by Umair Ali on 11/15/2017.
 */

public class Users {
    private String name, status, profile_image,phone,email,request_type,title,from,message,admin_image;
    private boolean isSelected = false;
    private Long time;

    public Users() {
    }

    public Users(String name, String status, String profile_image, String phone, String email, String request_type, String title, boolean isSelected) {
        this.name = name;
        this.status = status;
        this.profile_image = profile_image;
        this.phone = phone;
        this.email = email;
        this.request_type = request_type;
        this.title = title;
        this.isSelected = isSelected;
    }

    public Users(String name, String status, String profile_image, String phone, String email, String request_type, String title, String from, String message, String admin_image, boolean isSelected, Long time) {
        this.name = name;
        this.status = status;
        this.profile_image = profile_image;
        this.phone = phone;
        this.email = email;
        this.request_type = request_type;
        this.title = title;
        this.from = from;
        this.message = message;
        this.admin_image = admin_image;
        this.isSelected = isSelected;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdmin_image() {
        return admin_image;
    }

    public void setAdmin_image(String admin_image) {
        this.admin_image = admin_image;
    }
}

