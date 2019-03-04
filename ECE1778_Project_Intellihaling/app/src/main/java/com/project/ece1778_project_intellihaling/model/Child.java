package com.project.ece1778_project_intellihaling.model;

/**
 * Created by dell on 2019/2/27.
 */

public class Child {
    private String uid, parentUid, email, password, name, height, weight, iconPath, inhalerId;

    public Child(){

    }

    public Child(String uid, String parentUid, String email, String password, String name, String height, String weight, String iconPath, String inhalerId) {

        this.uid = uid;
        this.parentUid = parentUid;
        this.email = email;
        this.password = password;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.iconPath = iconPath;
        this.inhalerId = inhalerId;
    }

    public Child(String uid, String parentUid, String email, String password, String name) {

        this.uid = uid;
        this.parentUid = parentUid;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getInhalerId() {
        return inhalerId;
    }

    public void setInhalerId(String inhalerId) {
        this.inhalerId = inhalerId;
    }
}
