package com.project.ece1778_project_intellihaling.model;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;



import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by dell on 2019/2/26.
 */

public class AirflowDataManager {
    private String childUid;
    private String rawflowVolumn;
    private String rawTimestamp;

    private List<String> timestampList = new ArrayList<>();
    private List<String> flowVolumnList = new ArrayList<>();


    private List<Entry> chartEntryList = new ArrayList<>();
    public LineData data = new LineData();

    public AirflowDataManager() {
    }

    public AirflowDataManager(String childUid, String rawflowVolumn, String rawTimestamp) throws Exception {

        this.childUid = childUid;
        this.rawflowVolumn = rawflowVolumn;
        this.rawTimestamp = rawTimestamp;
        getPeakflowVolumnList(rawflowVolumn);
        getTimestampList(rawTimestamp);
        if (timestampList.size() != flowVolumnList.size()) {
            throw new Exception("The number of timestamp diffs from peakflowVolumn");
        } else {
            linechartDataSetBuilder(flowVolumnList, timestampList);
        }
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public List<String> getTimestampList() {
        return timestampList;
    }

    public String getRawflowVolumn() {
        return rawflowVolumn;
    }

    public void setRawflowVolumn(String rawflowVolumn) {
        this.rawflowVolumn = rawflowVolumn;
    }

    public String getRawTimestamp() {
        return rawTimestamp;
    }

    public void setRawTimestamp(String rawTimestamp) {
        this.rawTimestamp = rawTimestamp;
    }

    private void getTimestampList(String rawTimestamp) {
        String[] timeElements = rawTimestamp.split("/");
        for (int i = 0; i < timeElements.length; i++) {
            this.timestampList.add(timeElements[i]);
        }
       // xAxisLabelArray(timestampList);
    }

    private void getPeakflowVolumnList(String rawPeakflowVolumn) {
        String[] peakflowVolumnElements = rawPeakflowVolumn.split("/");
        for (int i = 0; i < peakflowVolumnElements.length; i++) {
            this.flowVolumnList.add(peakflowVolumnElements[i]);
        }
    }

    public String[] xAxisLabelArray(List<String> timestampList) {
        Long[] longTimestamp = new Long[timestampList.size()];
        for (int i = 0; i < longTimestamp.length; i++) {
            longTimestamp[i] = Long.parseLong(timestampList.get(i));
        }
        // realtime array is used to describe the X axis
        final String[] realtime = new String[timestampList.size()];
        //set the first time, which is the only full format time
        realtime[0] = timeToString(longTimestamp[0]);
        for (int i = 1; i < longTimestamp.length; i++) {
            realtime[i] = formatTime(longTimestamp[i]);
        }
        return realtime;
    }

    private void linechartDataSetBuilder(List<String> peakflowVolumnList, List<String> timestampList) {
        for (int i = 0; i < peakflowVolumnList.size(); i++) {
            this.chartEntryList.add(new Entry(i, Float.parseFloat(peakflowVolumnList.get(i))));
        }
        LineDataSet setComp1 = new LineDataSet(this.chartEntryList, "Last Record");
        //LINE COLOR
        setComp1.setColor(ColorTemplate.rgb("000000"));
        setComp1.setLineWidth(1.5f);
        setComp1.setValueTextSize(8f);
        data.addDataSet(setComp1);
    }

    public static String formatTime(long ms) {

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        //long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        //String strDay = day < 10 ? "0" + day : "" + day; //天
        String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
        String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
        String strSecond = second < 10 ? "0" + second : "" + second;//秒
        //String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
       // strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strHour + " : " + strMinute ;
    }

    private String timeToString(Long millisecond){

        Date d = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String s = sdf.format(d);
        return s;
    }

}
