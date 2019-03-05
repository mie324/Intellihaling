package com.project.ece1778_project_intellihaling.model;

/**
 * Created by dell on 2019/2/27.
 */

public class Inhaler {
    private String uId;
    private String firstUsageDate;
    private String margin;

    public Inhaler() {
    }

    public Inhaler(String uId, String firstUsageDate, String margin) {
        this.uId = uId;
        this.firstUsageDate = firstUsageDate;
        this.margin = margin;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
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
