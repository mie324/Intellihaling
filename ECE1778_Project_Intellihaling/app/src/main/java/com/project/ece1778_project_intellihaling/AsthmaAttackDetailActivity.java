package com.project.ece1778_project_intellihaling;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.FlowQueryManager;
import com.project.ece1778_project_intellihaling.model.RecyclerViewSpacesItemDecoration;
import com.project.ece1778_project_intellihaling.util.AttackRecordAdapter;
import com.project.ece1778_project_intellihaling.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class AsthmaAttackDetailActivity extends AppCompatActivity {
    private static final String TAG = "AsthmaAttackDetailActiv";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String mUid;
    private RecyclerView mRecyclerView;
    private LineChart mLineChartPeakflow;
    private LineChart mLineChartFEV;
    private List<FlowQueryManager> mAirflowDataManagerList =new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asthma_attack_detail);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mUid = mAuth.getUid();
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
        //we need to judge the role to judge child/parent
        //we here assume that the role is child

    }

    @Override
    protected void onStart() {
        super.onStart();
        //拿到符合条件的所以document的数据
        getData(new AsynchronousDealerInterface() {
            @Override
            public void listGenerator(List<FlowQueryManager> list) {
                //FlowQueryManager stores all messages from query docs
                mAirflowDataManagerList = list;
                //将attackTimestamp转化成真实时间 还有原始数据 排序 并存入List 之后交给adapter渲染
                AttackRecordAdapter adapter = new AttackRecordAdapter(getApplicationContext(), mAirflowDataManagerList);
                mRecyclerView.setAdapter(adapter);
                //对时间进行排序 并在chart上显示最近一次
                Collections.sort(mAirflowDataManagerList, new Comparator<FlowQueryManager>() {
                    @Override
                    public int compare(FlowQueryManager o1, FlowQueryManager o2) {
                        return o1.getAttackTimestamp().compareTo(o2.getAttackTimestamp());
                    }
                });
                setChartFlow(mAirflowDataManagerList,mLineChartFEV,mAirflowDataManagerList.get(0).getFev());
                setChartFlow(mAirflowDataManagerList,mLineChartPeakflow,mAirflowDataManagerList.get(0).getPeakflow());


            }
        });

    }

    private void setChartFlow(List<FlowQueryManager> list,LineChart mLineChart,String flowVolumn){

        try {
            AirflowDataManager airflowDataManager = new AirflowDataManager(list.get(0).getuId()
                    ,flowVolumn,list.get(0).getPeakflowAndfevTimestamp());
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
            xAxisMessageofChart(mLineChart,formatter);
            mLineChart.setData(airflowDataManager.data);
            mLineChart.invalidate();
            //just to show fev, not real fev
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void xAxisMessageofChart(LineChart lineChart,IAxisValueFormatter formatter) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextSize(15f);
    }


    public interface AsynchronousDealerInterface{
         void listGenerator(List<FlowQueryManager> list);
    }
    public void getData(final AsynchronousDealerInterface asynchronousDealer){
        mDatabase.collection("peakflow").whereEqualTo("childUid",mUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<FlowQueryManager> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        FlowQueryManager flowQueryManager = new FlowQueryManager(document.getString("childUid")
                                ,document.getString("attackTimestamp"),document.getString("attackTimestampYear")
                                ,document.getString("attackTimestampMonth"),document.getString("attackTimestampDay"),document.getString("peakflow")
                                ,document.getString("fev"),document.getString("peakflowAndfevTimestamp"));
                                list.add(flowQueryManager);
                    }

                    asynchronousDealer.listGenerator(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

}
