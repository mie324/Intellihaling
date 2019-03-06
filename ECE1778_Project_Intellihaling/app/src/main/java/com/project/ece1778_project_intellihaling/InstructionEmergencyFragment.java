package com.project.ece1778_project_intellihaling;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

public class InstructionEmergencyFragment extends Fragment {

    private static final String TAG = "InstructionEmergencyFra";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_PASS_INDEX = 2;
    private static final int FRAG_FAIL_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;

    //UI
    private TextView CurrentTimeView;

    //vars
    private StartActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (StartActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_instruction_emergency, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_time);

        setUp();

        return view;
    }

    private void setUp(){

        //set up start time
        String yyyy = OnceAttackRecordStatic.getAttackTimestampYear();
        String MM = OnceAttackRecordStatic.getAttackTimestampMonth();
        String dd = OnceAttackRecordStatic.getAttackTimestampDay();
        String HH = OnceAttackRecordStatic.getAttackTimestampHour();
        String mm = OnceAttackRecordStatic.getAttackTimestampMinute();
        String currentTime = "Start: " + yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm;
        CurrentTimeView.setText(currentTime);

    }


}
