package com.project.ece1778_project_intellihaling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecord;
import com.project.ece1778_project_intellihaling.model.RecyclerViewSpacesItemDecoration;
import com.project.ece1778_project_intellihaling.util.AttackRecordAdapter;
import com.project.ece1778_project_intellihaling.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;


public class AsthmaAttackDetailActivity extends AppCompatActivity {

    private static final String TAG = "AsthmaAttackDetailActiv";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String mUid;
    private String childUID;

    //widgets
    private TextView mNameTest;
    private RecyclerView mRecyclerView;
    private LineChart mLineChartPeakflow;
    private LineChart mLineChartFEV;

    private List<OnceAttackRecord> mAirflowDataManagerList = new ArrayList<>();

    private Intent mIntentFromRecyclerView;
    private Bundle mDataFromRecyclerView;

    private DocumentReference mDocRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asthma_attack_detail);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mNameTest = findViewById(R.id.chart_name_title);
        mRecyclerView = findViewById(R.id.recyclerView);
        mLineChartFEV = findViewById(R.id.chart_detail_fev);
        mLineChartPeakflow = findViewById(R.id.chart_detail_peakflow);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        mRecyclerView.setLayoutManager(layoutManager);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION, 10);//top padding
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION, 10);//bottom padding
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION, 10);//left padding
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION, 10);//right padding
        mRecyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));

        Intent intent = getIntent();
        if (intent.hasExtra("uId")) {

            //chart data from recyclerview
            mIntentFromRecyclerView = getIntent();
            mDataFromRecyclerView = mIntentFromRecyclerView.getExtras();

        } else if (intent.hasExtra("childUID")) {

            //chart data will be fetched from database
            childUID = intent.getStringExtra("childUID");

        }else{
            //exception
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(childUID != null){

            getData(new AsynchronousDealerInterface() {
                @Override
                public void listGenerator(List<OnceAttackRecord> list) {
                    mAirflowDataManagerList = list;

                    AttackRecordAdapter adapter =new AttackRecordAdapter(getApplicationContext(), mAirflowDataManagerList);
                    mRecyclerView.setAdapter(adapter);

                    Collections.sort(mAirflowDataManagerList, new Comparator<OnceAttackRecord>() {
                        @Override
                        public int compare(OnceAttackRecord o1, OnceAttackRecord o2) {
                            return o2.getAttackTimestamp().compareTo(o1.getAttackTimestamp());
                        }
                    });

                    setChartFlow(mAirflowDataManagerList, mLineChartFEV, mAirflowDataManagerList.get(0).getFev());
                    setChartFlow(mAirflowDataManagerList, mLineChartPeakflow, mAirflowDataManagerList.get(0).getPeakflow());

                    setTitleName(new FireStoreCallback() {
                        @Override
                        public void onCallback() {
                            Log.d(TAG, "set title name onCallback: name: " + mNameTest.getText().toString());
                        }
                    });
                }
            });

        }else{

            childUID = mDataFromRecyclerView.getString("uId");
            getData(new AsynchronousDealerInterface() {
                @Override
                public void listGenerator(List<OnceAttackRecord> list) {
                    //OnceAttackRecord stores all messages from query docs
                    mAirflowDataManagerList = list;

                    //将attackTimestamp转化成真实时间 还有原始数据 排序 并存入List 之后交给adapter渲染
                    AttackRecordAdapter adapter = new AttackRecordAdapter(getApplicationContext(), mAirflowDataManagerList);

                    mRecyclerView.setAdapter(adapter);
                    String peakflowVolumn = mDataFromRecyclerView.getString("peakFlow");
                    String fevVolumn = mDataFromRecyclerView.getString("fev");
                    String timestamp = mDataFromRecyclerView.getString("attackTimeStamp");
                    String uId = mDataFromRecyclerView.getString("uId");
                    setChartDataFromRecyclerView(uId, mLineChartFEV, fevVolumn, timestamp);
                    setChartDataFromRecyclerView(uId, mLineChartPeakflow, peakflowVolumn, timestamp);

                    setTitleName(new FireStoreCallback() {
                        @Override
                        public void onCallback() {
                            Log.d(TAG, "set title name onCallback: name: " + mNameTest.getText().toString());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setChartDataFromRecyclerView(String uId, LineChart mLineChart, String flowVolumn, String timestamp) {
        try {
            AirflowDataManager airflowDataManager = new AirflowDataManager(uId
                    , flowVolumn, timestamp);

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

            xAxisMessageofChart(mLineChart, formatter);
            mLineChart.setData(airflowDataManager.data);
            mLineChart.invalidate();
            //just to show fev, not real fev

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setChartFlow(List<OnceAttackRecord> list, LineChart mLineChart, String flowVolumn) {

        try {
            AirflowDataManager airflowDataManager = new AirflowDataManager(list.get(0).getuId()
                    , flowVolumn, list.get(0).getPeakflowAndfevTimestamp());

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

            xAxisMessageofChart(mLineChart, formatter);
            mLineChart.setData(airflowDataManager.data);
            mLineChart.invalidate();
            //just to show fev, not real fev

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void xAxisMessageofChart(LineChart lineChart, IAxisValueFormatter formatter) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextSize(15f);
        xAxis.setDrawGridLines(false);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        yAxis.setLabelCount(2,false);
    }


    public interface AsynchronousDealerInterface {
        void listGenerator(List<OnceAttackRecord> list);
    }

    public void getData(final AsynchronousDealerInterface asynchronousDealer) {

        mDatabase.collection("attackRecord").whereEqualTo("childUid", childUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<OnceAttackRecord> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String tsTmp = document.getString("peakflowAndFevTimestamp");
                        if(tsTmp.contains("/")){
                            OnceAttackRecord onceAttackRecord = new OnceAttackRecord(document.getString("childUid"),
                                    document.getString("attackTimestamp"), document.getString("attackTimestampYear"),
                                    document.getString("attackTimestampMonth"), document.getString("attackTimestampDay"),
                                    document.getString("peakflow"),
                                    document.getString("fev"),
                                    document.getString("peakflowAndFevTimestamp"));

                            list.add(onceAttackRecord);
                        }
                    }

                    asynchronousDealer.listGenerator(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void setTitleName(final FireStoreCallback fireStoreCallback) {

        //only way to check what role of uid
        final DocumentReference docRef = mDatabase.collection("child").document(childUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        mNameTest.setText((String)document.get("name"));
                    } else {
                        mNameTest.setText((String)document.get("name"));
                    }

                    fireStoreCallback.onCallback();

                } else {
                    Log.d(TAG, "detect role onComplete: task fails: ", task.getException());
                }
            }
        });
    }

    private interface FireStoreCallback {
        void onCallback();
    }

}
