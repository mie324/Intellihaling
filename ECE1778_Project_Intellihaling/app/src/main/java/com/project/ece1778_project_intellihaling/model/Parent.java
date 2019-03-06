package com.project.ece1778_project_intellihaling.model;

public class Parent {

    private String uid, childsUid, email, iconPath, name, password;

    public Parent(){

    }

    public Parent(String uid, String email, String name, String password, String iconPath, String childsUid) {

        this.uid = uid;

        this.email = email;
        this.name = name;
        this.password = password;
        this.iconPath = iconPath;

        this.childsUid = childsUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChildsUid() {
        return childsUid;
    }

    public void setChildsUid(String childsUid) {
        this.childsUid = childsUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
