package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.FlowQueryManager;
import com.project.ece1778_project_intellihaling.util.BottomNavigationViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by dell on 2019/2/27.
 */

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private String uID;

    private LineChart mLineChartPeakflow;
    private LineChart mLineChartFEV;

    private Context mContext;
    private List<FlowQueryManager> mFlowQueryManagerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mContext = HomeActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mLineChartPeakflow = (LineChart) findViewById(R.id.chart_peakflow);
        mLineChartFEV = (LineChart) findViewById(R.id.chart_fev);
        //设置可缩放
        mLineChartPeakflow.setScaleEnabled(true);
        setupBottomNavigationView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //assume that the current user is child
            uID = currentUser.getUid();
            //fetch corresponding and show data on chart
            fetchDocsFromDB(uID);

        } else {
            enterLoginActivity();
        }


    }

    private void fetchDocsFromDB(String uID) {
        mFlowQueryManagerList = new ArrayList<>();
        mDatabase.collection("peakflow").whereEqualTo("childUid", uID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                                FlowQueryManager flowQueryManager = new FlowQueryManager(document.getString("childUid")
                                        , document.getString("attackTimestamp"), document.getString("attackTimestampYear")
                                        , document.getString("attackTimestampMonth"), document.getString("attackTimestampDay"), document.getString("peakflow")
                                        , document.getString("fev"), document.getString("peakflowAndfevTimestamp"));
                                mFlowQueryManagerList.add(flowQueryManager);
                                if (mFlowQueryManagerList.size() == task.getResult().size()) {
                                    Collections.sort(mFlowQueryManagerList, new Comparator<FlowQueryManager>() {
                                        public int compare(FlowQueryManager o1, FlowQueryManager o2) {
                                            return o1.getAttackTimestamp().compareTo(o2.getAttackTimestamp());
                                        }
                                    });
                                    fetchLatestDocFromDBAndSetChart(mFlowQueryManagerList.get(0));
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    private void fetchLatestDocFromDBAndSetChart(FlowQueryManager flowQueryManager) {
        mDatabase.collection("peakflow").whereEqualTo("attackTimestamp", flowQueryManager.getAttackTimestamp())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                FlowQueryManager flowQueryManager = new FlowQueryManager(document.getString("childUid")
                                        , document.getString("attackTimestamp"), document.getString("attackTimestampYear")
                                        , document.getString("attackTimestampMonth"), document.getString("attackTimestampDay"), document.getString("peakflow")
                                        , document.getString("fev"), document.getString("peakflowAndfevTimestamp"));
                                try {
                                    AirflowDataManager airflowDataManager = new AirflowDataManager(flowQueryManager.getuId()
                                            , flowQueryManager.getPeakflow(), flowQueryManager.getPeakflowAndfevTimestamp());
                                    //the content of timesatmpList array like this {2019-2-17 22:46,22:57,23:15}
                                    List<String> timestampList = airflowDataManager.getTimestampList();
                                    //formatter is used for customize X axis according to timestampList;
                                    final String[] strings = airflowDataManager.xAxisLabelArray(timestampList);
                                    IAxisValueFormatter formatter = new IAxisValueFormatter() {
                                        @Override
                                        public String getFormattedValue(float value, AxisBase axis) {
                                            return strings[(int) value];
                                        }
                                    };
                                    //formatter is for set layout for chart
                                    xAxisMessageofChart(mLineChartPeakflow, formatter);
                                    mLineChartPeakflow.setData(airflowDataManager.data);
                                    mLineChartPeakflow.invalidate();
                                    //just to show fev, not real fev
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    AirflowDataManager airflowDataManager = new AirflowDataManager(flowQueryManager.getuId()
                                            , flowQueryManager.getFev(), flowQueryManager.getPeakflowAndfevTimestamp());
                                    //the content of timesatmpList array like this {2019-2-17 22:46,22:57,23:15}
                                    List<String> timestampList = airflowDataManager.getTimestampList();
                                    //formatter is used for customize X axis according to timestampList;
                                    final String[] strings = airflowDataManager.xAxisLabelArray(timestampList);
                                    IAxisValueFormatter formatter = new IAxisValueFormatter() {
                                        @Override
                                        public String getFormattedValue(float value, AxisBase axis) {
                                            return strings[(int) value];
                                        }
                                    };
                                    xAxisMessageofChart(mLineChartFEV, formatter);
                                    mLineChartFEV.setData(airflowDataManager.data);
                                    mLineChartFEV.invalidate();
                                    //just to show fev, not real fev
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                    }
                });
    }

    private void enterLoginActivity() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void xAxisMessageofChart(LineChart lineChart, IAxisValueFormatter formatter) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextSize(15f);
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.layoutbottomNavBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public void startAsthmaAttackDetailActivity(View view) {
        Intent intent = new Intent(HomeActivity.this, AsthmaAttackDetailActivity.class);
        startActivity(intent);
    }

    public void startAsthmaAttackDetailActivity1(View view) {
        startAsthmaAttackDetailActivity(view);
    }
    //when you click on chart, the page will jump to AsthmaAttackDetailActivity


}
