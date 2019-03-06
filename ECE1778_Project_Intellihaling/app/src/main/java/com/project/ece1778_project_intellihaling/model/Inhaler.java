package com.project.ece1778_project_intellihaling.model;

public class Inhaler {

    private String childUid, firstUsageDate, margin;

    public Inhaler() {
    }

    public Inhaler(String childUid, String firstUsageDate, String margin) {
        this.childUid = childUid;
        this.firstUsageDate = firstUsageDate;
        this.margin = margin;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getFirstUsageDate() {
        return firstUsageDate;
    }

    public void setFirstUsageDate(String firstUsageDate) {
        this.firstUsageDate = firstUsageDate;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }


}
