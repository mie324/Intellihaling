package com.project.ece1778_project_intellihaling.model;

/**
 * Created by dell on 2019/3/4.
 */

public class InhalerManager {
    private String uId;
    private String firstUsageDate;
    private String margin;

    public InhalerManager() {
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

    public InhalerManager(Inhaler inhaler){
        this.uId = inhaler.getuId();
        this.firstUsageDate = inhaler.getFirstUsageDate();
        this.margin = inhaler.getMargin();
    }
    public String calculateRemainingDays(){
        Long firstDate = Long.parseLong(this.firstUsageDate);
        Long currentTimeMillis = System.currentTimeMillis();
        Long remainingMillis = currentTimeMillis - firstDate;
        String remainingDays = formatTime(remainingMillis);
        return remainingDays;
    }
    public static String formatTime(long ms) {

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
//        long hour = (ms - day * dd) / hh;
//        long minute = (ms - day * dd - hour * hh) / mi;
//        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        //long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day; //天
//        String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
//        String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
//        String strSecond = second < 10 ? "0" + second : "" + second;//秒
        //String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
        // strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strDay;
    }
}
