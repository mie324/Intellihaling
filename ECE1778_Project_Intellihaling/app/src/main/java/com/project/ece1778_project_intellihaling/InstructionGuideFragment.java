package com.project.ece1778_project_intellihaling;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InstructionGuideFragment extends Fragment {

    private static final String TAG = "InstructionGuideFragmen";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_PASS_INDEX = 2;
    private static final int FRAG_FAIL_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;

    private static final int ATTACK_COUNT_LIMIT = 10;

    //UI
    private TextView CurrentTimeView;
    private EditText PKEditText, FEVEditText;
    private Button confirmBtn;

    private StartActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if(bundle != null){

        }

        mActivity = (StartActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_instruction_guide, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_time);

        PKEditText = (EditText)view.findViewById(R.id.instruction_guide_pfnum_enter);
        FEVEditText = (EditText)view.findViewById(R.id.instruction_guide_fevnum_enter);

        confirmBtn = (Button)view.findViewById(R.id.btn_instruction_guide_enter_done);

        setUp();

        btnOnClick();

        return view;
    }

    public void setUp(){

        //set up showing time
        String yyyy = OnceAttackRecordStatic.getAttackTimestampYear();
        String MM = OnceAttackRecordStatic.getAttackTimestampMonth();
        String dd = OnceAttackRecordStatic.getAttackTimestampDay();
        String HH = OnceAttackRecordStatic.getAttackTimestampHour();
        String mm = OnceAttackRecordStatic.getAttackTimestampMinute();
        String currentTime = "Start: " + yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm;
        CurrentTimeView.setText(currentTime);

    }

    public void btnOnClick(){

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PKEditText.setError(null);
                FEVEditText.setError(null);

                String pkNum = PKEditText.getText().toString();
                String fevNum = FEVEditText.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(pkNum)) {
                    PKEditText.setError(getString(R.string.error_field_required));
                    focusView = PKEditText;
                    cancel = true;
                }

                if (TextUtils.isEmpty(fevNum)) {
                    FEVEditText.setError(getString(R.string.error_field_required));
                    focusView = FEVEditText;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();

                } else {

                    //record pk
                    String pkNumOrg = OnceAttackRecordStatic.getPeakflow();
                    String pkNumNew = "";
                    if(pkNumOrg == null)
                        pkNumNew = pkNum;
                    else
                        pkNumNew = pkNumOrg + "/" + pkNum;
                    OnceAttackRecordStatic.setPeakflow(pkNumNew);

                    //record fev
                    String fevNumOrg = OnceAttackRecordStatic.getFev();
                    String fevNumNew = "";
                    if(fevNumOrg == null)
                       fevNumNew = fevNum;
                    else
                        fevNumNew = fevNumOrg + "/" + fevNum;
                    OnceAttackRecordStatic.setFev(fevNumNew);

                    //record time
                    long timeMillis = System.currentTimeMillis();
                    String currentts = String.valueOf(timeMillis);
                    String tsOrg = OnceAttackRecordStatic.getPeakflowAndFevTimestamp();
                    String tsNew = "";
                    if(tsOrg == null)
                        tsNew = currentts;
                    else
                        tsNew = tsOrg + "/" + currentts;
                    OnceAttackRecordStatic.setPeakflowAndFevTimestamp(tsNew);

                    //record inhalor margin
                    int currentMargin = Integer.valueOf(OnceAttackRecordStatic.getInhalorMargin());
                    int newMargin = currentMargin - 2;
                    OnceAttackRecordStatic.setInhalorMargin(String.valueOf(newMargin));

                    //record count
                    int currentCount = OnceAttackRecordStatic.getCount();
                    currentCount++;
                    OnceAttackRecordStatic.setCount(currentCount);

                    //if count over 10, should direct to emergency
                    if(currentCount > ATTACK_COUNT_LIMIT){

                        mActivity.setViewPager(FRAG_EMER_INDEX);

                    }else{
                        String childHeight = OnceAttackRecordStatic.getChildHeight();

                        if(checkAttack(childHeight, pkNum, fevNum)){
                            mActivity.setViewPager(FRAG_PASS_INDEX);
                        }else{
                            mActivity.setViewPager(FRAG_FAIL_INDEX);
                        }
                    }

                }
            }
        });
    }

    private boolean checkAttack(String height, String pkNum, String fevNum){

        //compare to the form and check whether it is a valid attack, fake data
        if(pkNum.equals("3500") && fevNum.equals("3600")){
            return true;
        }else{
            return false;
        }
    }

}
