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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.Inhaler;
import com.project.ece1778_project_intellihaling.model.InhalerManager;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecord;
import com.project.ece1778_project_intellihaling.util.BottomNavigationViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private String role;
    private String childUID;

    private LineChart mLineChartPeakflow;
    private LineChart mLineChartFEV;
    private TextView mMargin;
    private TextView mRemainingDays;
    private Context mContext;
    private List<OnceAttackRecord> mOnceAttackRecordList;

    private Inhaler mInhaler = new Inhaler();

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
        mMargin=findViewById(R.id.puff_remaining_textView);
        mRemainingDays = findViewById(R.id.expiry_date_textview);
        //设置可缩放
        mLineChartPeakflow.setScaleEnabled(true);

        role = "";
        childUID = "";
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
            detectRole(new FireStoreCallback() {
                @Override
                public void onCallback() {
                    Log.d(TAG, "detect role onCallback: + " + role + " child:" +  childUID);

                    //fetch corresponding and show data on chart
                    fetchDocsFromDB(childUID);
                    getInhalerInfo(new InhalerInfoSetInterface() {
                        @Override
                        public void inhalerSetter(Inhaler inhaler) {
                            mInhaler = inhaler;
                            inhalerLayout(mInhaler);
                        }
                    });
                }
            });

        } else {
            enterLoginActivity();
        }
    }

    private void detectRole(final FireStoreCallback fireStoreCallback){

        //only way to check what role of uid
        final DocumentReference docRef = mDatabase.collection("parent").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        role = "parent";
                        childUID = (String)document.get("childsUid");
                    }else{
                        role = "child";
                        childUID = uID;
                    }

                    fireStoreCallback.onCallback();

                }else {
                    Log.d(TAG, "detect role onComplete: task fails: ", task.getException() );
                }
            }
        });
    }

    private interface FireStoreCallback{
        void onCallback();
    }

    private void inhalerLayout(Inhaler inhaler){
        InhalerManager inhalerManager = new InhalerManager(inhaler);
        String remainingDays = inhalerManager.calculateRemainingDays();
        String margin = inhalerManager.getMargin();
        mMargin.setText(margin);
        mRemainingDays.setText(remainingDays);
    }
    public interface InhalerInfoSetInterface {
        void inhalerSetter(Inhaler inhaler);
    }

    public void getInhalerInfo(final InhalerInfoSetInterface inhalerInfoSetInterface) {
        mDatabase.collection("inhaler").whereEqualTo("childUid", childUID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Inhaler inhaler = new Inhaler();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                inhaler.setFirstUsageDate(document.getString("firstUsageDate"));
                                inhaler.setMargin(document.getString("margin"));
                                inhaler.setuId(document.getString("childUid"));
                            }
                            inhalerInfoSetInterface.inhalerSetter(inhaler);
                        }

                    }
                });
    }

    private void fetchDocsFromDB(String childUID) {
        mOnceAttackRecordList = new ArrayList<>();
        mDatabase.collection("peakflow").whereEqualTo("childUid", childUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                                OnceAttackRecord onceAttackRecord = new OnceAttackRecord(document.getString("childUid")
                                        , document.getString("attackTimestamp"), document.getString("attackTimestampYear")
                                        , document.getString("attackTimestampMonth"), document.getString("attackTimestampDay"), document.getString("peakflow")
                                        , document.getString("fev"), document.getString("peakflowAndfevTimestamp"));
                                mOnceAttackRecordList.add(onceAttackRecord);
                                if (mOnceAttackRecordList.size() == task.getResult().size()) {
                                    Collections.sort(mOnceAttackRecordList, new Comparator<OnceAttackRecord>() {
                                        public int compare(OnceAttackRecord o1, OnceAttackRecord o2) {
                                            return o1.getAttackTimestamp().compareTo(o2.getAttackTimestamp());
                                        }
                                    });
                                    fetchLatestDocFromDBAndSetChart(mOnceAttackRecordList.get(0));
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    private void fetchLatestDocFromDBAndSetChart(OnceAttackRecord onceAttackRecord) {
        mDatabase.collection("peakflow").whereEqualTo("attackTimestamp", onceAttackRecord.getAttackTimestamp())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                OnceAttackRecord onceAttackRecord = new OnceAttackRecord(document.getString("childUid")
                                        , document.getString("attackTimestamp"), document.getString("attackTimestampYear")
                                        , document.getString("attackTimestampMonth"), document.getString("attackTimestampDay"), document.getString("peakflow")
                                        , document.getString("fev"), document.getString("peakflowAndfevTimestamp"));
                                try {
                                    AirflowDataManager airflowDataManager = new AirflowDataManager(onceAttackRecord.getuId()
                                            , onceAttackRecord.getPeakflow(), onceAttackRecord.getPeakflowAndfevTimestamp());
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
                                    AirflowDataManager airflowDataManager = new AirflowDataManager(onceAttackRecord.getuId()
                                            , onceAttackRecord.getFev(), onceAttackRecord.getPeakflowAndfevTimestamp());
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
        intent.putExtra("childUID", childUID);
        startActivity(intent);
    }

    public void startAsthmaAttackDetailActivity1(View view) {
        startAsthmaAttackDetailActivity(view);
    }
    //when you click on chart, the page will jump to AsthmaAttackDetailActivity


}
