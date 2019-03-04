package com.project.ece1778_project_intellihaling.util;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.FlowQueryManager;
import com.project.ece1778_project_intellihaling.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dell on 2019/3/3.
 */

public class AttackRecordAdapter extends RecyclerView.Adapter<AttackRecordAdapter.ViewHolder> {
    private Context mContext;
    private FirebaseAuth mAuth;
    private String mUid;
    private List<FlowQueryManager> attackDocsList;

    public AttackRecordAdapter(Context context,List<FlowQueryManager> attackDocsList) {
        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getUid();
        this.attackDocsList = attackDocsList;
        //judge if this user is child
        //we assume that the user is a child
        //then we fetch all record of the child from database
        Collections.sort(attackDocsList, new Comparator<FlowQueryManager>() {
            @Override
            public int compare(FlowQueryManager o1, FlowQueryManager o2) {
                return o1.getAttackTimestamp().compareTo(o2.getAttackTimestamp());
            }
        });
    }


    @NonNull
    @Override

    public AttackRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_for_date_in_recyclerview, null, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final AttackRecordAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.btn.setText(attackDocsList.get(i).getAttackTimestampday()+"/"+attackDocsList.get(i).getAttackTimestampMonth()
                +"/"+attackDocsList.get(i).getAttackTimestampYear());
        viewHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlowQueryManager flowQueryManager = attackDocsList.get(i);
                setChartFlow(flowQueryManager,viewHolder.mLineChartFEV,flowQueryManager.getFev());
                setChartFlow(flowQueryManager,viewHolder.mLineChartPeakflow,flowQueryManager.getPeakflow());
            }
        });
    }
    private void setChartFlow(FlowQueryManager flowQueryManager,LineChart mLineChart,String flowVolumn){

        try {
            AirflowDataManager airflowDataManager = new AirflowDataManager(flowQueryManager.getuId()
                    ,flowVolumn,flowQueryManager.getPeakflowAndfevTimestamp());
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

    @Override
    public int getItemCount() {
        return attackDocsList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private Button btn;
        private LineChart mLineChartPeakflow;
        private LineChart mLineChartFEV;
        public ViewHolder(View itemView) {
            super(itemView);
            btn = (Button)itemView.findViewById(R.id.button_in_recyclerView);
            mLineChartPeakflow= itemView.findViewById(R.id.chart_detail_peakflow);
            mLineChartPeakflow= itemView.findViewById(R.id.chart_detail_fev);
        }
    }
}
