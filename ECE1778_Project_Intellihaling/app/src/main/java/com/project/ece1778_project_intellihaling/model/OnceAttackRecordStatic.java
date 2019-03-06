package com.project.ece1778_project_intellihaling.model;

public class OnceAttackRecordStatic {

    private static String childUid;
    private static String childHeight;
    private static String inhalorMargin;
    private static String attackTimestamp, attackTimestampMinute, attackTimestampHour, attackTimestampDay, attackTimestampMonth, attackTimestampYear;
    private static String fev, peakflow, peakflowAndFevTimestamp;
    private static int count;

    public static String getChildHeight() {
        return childHeight;
    }

    public static void setChildHeight(String childHeight) {
        OnceAttackRecordStatic.childHeight = childHeight;
    }

    public static String getInhalorMargin() {
        return inhalorMargin;
    }

    public static void setInhalorMargin(String inhalorMargin) {
        OnceAttackRecordStatic.inhalorMargin = inhalorMargin;
    }

    public static String getChildUid() {
        return childUid;
    }

    public static void setChildUid(String childUid) {
        OnceAttackRecordStatic.childUid = childUid;
    }

    public static String getAttackTimestamp() {
        return attackTimestamp;
    }

    public static String getAttackTimestampMinute() {
        return attackTimestampMinute;
    }

    public static void setAttackTimestampMinute(String attackTimestampMinute) {
        OnceAttackRecordStatic.attackTimestampMinute = attackTimestampMinute;
    }

    public static String getAttackTimestampHour() {
        return attackTimestampHour;
    }

    public static void setAttackTimestampHour(String attackTimestampHour) {
        OnceAttackRecordStatic.attackTimestampHour = attackTimestampHour;
    }

    public static void setAttackTimestamp(String attackTimestamp) {
        OnceAttackRecordStatic.attackTimestamp = attackTimestamp;
    }

    public static String getAttackTimestampDay() {
        return attackTimestampDay;
    }

    public static void setAttackTimestampDay(String attackTimestampDay) {
        OnceAttackRecordStatic.attackTimestampDay = attackTimestampDay;
    }

    public static String getAttackTimestampMonth() {
        return attackTimestampMonth;
    }

    public static void setAttackTimestampMonth(String attackTimestampMonth) {
        OnceAttackRecordStatic.attackTimestampMonth = attackTimestampMonth;
    }

    public static String getAttackTimestampYear() {
        return attackTimestampYear;
    }

    public static void setAttackTimestampYear(String attackTimestampYear) {
        OnceAttackRecordStatic.attackTimestampYear = attackTimestampYear;
    }

    public static String getFev() {
        return fev;
    }

    public static void setFev(String fev) {
        OnceAttackRecordStatic.fev = fev;
    }

    public static String getPeakflow() {
        return peakflow;
    }

    public static void setPeakflow(String peakflow) {
        OnceAttackRecordStatic.peakflow = peakflow;
    }

    public static String getPeakflowAndFevTimestamp() {
        return peakflowAndFevTimestamp;
    }

    public static void setPeakflowAndFevTimestamp(String peakflowAndFevTimestamp) {
        OnceAttackRecordStatic.peakflowAndFevTimestamp = peakflowAndFevTimestamp;
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        OnceAttackRecordStatic.count = count;
    }
}
