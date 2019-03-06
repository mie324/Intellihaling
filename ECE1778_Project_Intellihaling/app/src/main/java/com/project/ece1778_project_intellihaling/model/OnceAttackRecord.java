package com.project.ece1778_project_intellihaling.model;

public class OnceAttackRecord {
    private String childUid;
    private String attackTimestamp;
    private String attackTimestampYear;
    private String attackTimestampMonth;
    private String attackTimestampday;
    private String peakflow;
    private String fev;
    private String peakflowAndfevTimestamp;

    public OnceAttackRecord(String uId, String attackTimeStamp, String attackTimestampYear, String attackTimestampMonth, String attackTimestampday, String peakflow, String fev, String peakflowAndfevTimestamp) {
        this.childUid = uId;
        this.attackTimestamp = attackTimeStamp;
        this.attackTimestampYear = attackTimestampYear;
        this.attackTimestampMonth = attackTimestampMonth;
        this.attackTimestampday = attackTimestampday;
        this.peakflow = peakflow;
        this.fev = fev;
        this.peakflowAndfevTimestamp = peakflowAndfevTimestamp;
    }

    public String getAttackTimestamp() {
        return attackTimestamp;
    }

    public void setAttackTimestamp(String attackTimestamp) {
        this.attackTimestamp = attackTimestamp;
    }

    public OnceAttackRecord() {

    }

    public String getuId() {
        return childUid;
    }

    public void setuId(String uId) {
        this.childUid = uId;
    }

    public String getAttackTimestampYear() {
        return attackTimestampYear;
    }

    public void setAttackTimestampYear(String attackTimestampYear) {
        this.attackTimestampYear = attackTimestampYear;
    }

    public String getAttackTimestampMonth() {
        return attackTimestampMonth;
    }

    public void setAttackTimestampMonth(String attackTimestampMonth) {
        this.attackTimestampMonth = attackTimestampMonth;
    }

    public String getAttackTimestampday() {
        return attackTimestampday;
    }

    public void setAttackTimestampday(String attackTimestampday) {
        this.attackTimestampday = attackTimestampday;
    }

    public String getPeakflow() {
        return peakflow;
    }

    public void setPeakflow(String peakflow) {
        this.peakflow = peakflow;
    }

    public String getFev() {
        return fev;
    }

    public void setFev(String fev) {
        this.fev = fev;
    }

    public String getPeakflowAndfevTimestamp() {
        return peakflowAndfevTimestamp;
    }

    public void setPeakflowAndfevTimestamp(String peakflowAndfevTimestamp) {
        this.peakflowAndfevTimestamp = peakflowAndfevTimestamp;
    }
}
