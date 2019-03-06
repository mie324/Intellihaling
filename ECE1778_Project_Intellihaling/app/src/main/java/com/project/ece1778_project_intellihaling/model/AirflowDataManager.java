package com.project.ece1778_project_intellihaling.model;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

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
        generateTimestampList(rawTimestamp);
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

    private void generateTimestampList(String rawTimestamp) {
        String[] timeElements = rawTimestamp.split("/");
        Log.d(TAG, "generateTimestampList: ");
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
    // this method return an array which give formatted time
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
            realtime[i] = timeToStringInShort(longTimestamp[i]);
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


    private String timeToString(Long millisecond){

        Date d = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String s = sdf.format(d);
        return s;
    }
    private String timeToStringInShort(Long millisecond){

        Date d = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String s = sdf.format(d);
        return s;
    }

}
