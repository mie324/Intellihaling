package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private String uID;
    private LineChart mLineChartPeakflow;
    private LineChart mLineChartFEV;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mLineChartPeakflow =(LineChart) findViewById(R.id.chart_peakflow);
        //设置可缩放
        mLineChartPeakflow.setScaleEnabled(true);
        mLineChartFEV =(LineChart) findViewById(R.id.chart_fev);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null){
//            uID = currentUser.getUid();
//        }else{
//            enterLoginActivity();
//        }
        //fetch chart data for chart_peakflow & chart_fev from database
        //set data to charts
        String uid = "woshimayuan";
        String timestamp = "1550443617571/1550443698780/1550445098733/1550613549419";
        String peakflow = "4900/4800/3300/3600";
        try {
            AirflowDataManager airflowDataManager = new AirflowDataManager(uid,peakflow,timestamp);
            //the content of timesatmpList array like this {2019-2-17 22:46,22:57,23:15}
            List<String> timestampList = airflowDataManager.getTimestampList();
            //formatter is used for customize X axis according to timestampList;
            final String[] strings = airflowDataManager.xAxisLabelArray(timestampList);
            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return strings[(int) value];
                }

                // we don't draw numbers, so no decimal digits needed
                //@Override
                public int getDecimalDigits() {  return 0; }
            };
            XAxis xAxis = mLineChartPeakflow.getXAxis();
            
            xAxis.setEnabled(true);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(formatter);
            YAxis yAxis = mLineChartPeakflow.getAxisLeft();
            yAxis.setTextSize(15f);
            mLineChartPeakflow.setData(airflowDataManager.data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void enterLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
