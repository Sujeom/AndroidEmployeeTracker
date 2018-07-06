package com.example.marti.employeetracker;

import android.graphics.Bitmap;

/**
 * Created by marti on 11/10/2017.
 */


public class Worker {

    //needed variables
    private String username = null;
    private String password = null;
    private String firstName = null;
    private String lastName = null;
    private String adminBit = null;
    private String groupId = null;
    private String adminId = null;
    private String dOB = null;
    private Integer empId = null;
    private Double latitude = null;
    private Double longitude = null;
    private String photo = null;
    private Boolean visible = true;
    private Bitmap bitmapImage = null;


    //Empty constructor
    public Worker(String user, String pass, String first, String last, String adm, String aId, String gId, String dob, int emp, Bitmap bitmapImage)
    {
        this.username = user;
        this.password = pass;
        this.firstName = first;
        this.lastName = last;
        this.adminBit = adm;
        this.groupId = gId;
        this.adminId = aId;
        this.dOB = dob;
        this.setEmpId(emp);
        this.photo = "nothing";
        this.visible = true;
        this.longitude = 0.0;
        this.latitude = 0.0;
        this.bitmapImage = bitmapImage;

    }

    public Worker()
    {

    }


    //getting usersname
    public String getUsername() {
        return username;
    }
    //setting username
    public void setUsername(String username) {
        this.username = username;
    }
    //getting latitude
    public Double getLatitude() {
        return latitude;
    }
    //setting latitude
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    //gtting longitude
    public Double getLongitude() {
        return longitude;
    }
    //setting longitude
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    //getting photo
    public String getPhoto() {
        return photo;
    }
    //setting photo
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    //setting password
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAdminBit() {
        return adminBit;
    }

    public void setAdminBit(String admin) {
        this.adminBit = admin;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getdOB() {
        return dOB;
    }

    public void setdOB(String dOB) {
        this.dOB = dOB;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }



    public Bitmap getBitmapImage() {
        return bitmapImage;
    }

    public void setBitmapImage(Bitmap bitmapImage) {
        this.bitmapImage = bitmapImage;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
