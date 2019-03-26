package com.project.ece1778_project_intellihaling.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.project.ece1778_project_intellihaling.AsthmaAttackDetailActivity;
import com.project.ece1778_project_intellihaling.R;
import com.project.ece1778_project_intellihaling.model.AirflowDataManager;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecord;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AttackRecordAdapter extends RecyclerView.Adapter<AttackRecordAdapter.ViewHolder> {

    private Context mContext;
    private FirebaseAuth mAuth;
    private String mUid;
    private List<OnceAttackRecord> attackDocsList;
    private Activity mActivity;

    public AttackRecordAdapter(Context context,Context activityContext, List<OnceAttackRecord> attackDocsList) {
        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getUid();
        this.attackDocsList = attackDocsList;
        mActivity = (Activity) activityContext;
        //judge if this user is child
        //we assume that the user is a child
        //then we fetch all record of the child from database
        Collections.sort(attackDocsList, new Comparator<OnceAttackRecord>() {
            @Override
            public int compare(OnceAttackRecord o1, OnceAttackRecord o2) {
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
        viewHolder.btn.setText(timeToString(Long.parseLong(attackDocsList.get(i).getAttackTimestamp())));
        viewHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AsthmaAttackDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("peakFlow",attackDocsList.get(i).getPeakflow());
                bundle.putString("fev",attackDocsList.get(i).getFev());
                bundle.putString("attackTimeStamp",attackDocsList.get(i).getPeakflowAndfevTimestamp());
                bundle.putString("uId",attackDocsList.get(i).getuId());
                intent.putExtras(bundle);
                mActivity.finish();

                mContext.startActivity(intent);

            }
        });
    }



    private String timeToString(Long millisecond){

        Date d = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String s = sdf.format(d);
        return s;
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
        }
    }
}
