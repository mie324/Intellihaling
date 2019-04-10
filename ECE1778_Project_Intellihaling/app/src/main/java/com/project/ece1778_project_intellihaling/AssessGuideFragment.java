package com.project.ece1778_project_intellihaling;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.ece1778_project_intellihaling.model.AttackReferenceStatic;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import static android.app.Activity.RESULT_OK;

public class AssessGuideFragment extends Fragment {

    private static final String TAG = "AssessGuideFragmen";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;

    private static final int FRAG_YELLOW_INDEX = 2;
    private static final int FRAG_RED_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;

    private static final int REQUEST_OK = 0;

    private View view;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String childUID;

    //UI
    private TextView CurrentTimeView;
    private EditText PKEditText, FEVEditText;
    private Button confirmBtn;

    private AssessActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if(bundle != null){

        }

        mActivity = (AssessActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_assess_guide, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.assess_guide_time);

        PKEditText = (EditText)view.findViewById(R.id.assess_guide_pfnum_enter);
        FEVEditText = (EditText)view.findViewById(R.id.assess_guide_fevnum_enter);

        confirmBtn = (Button)view.findViewById(R.id.btn_assess_guide_enter_done);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        setMenuVisibility(false);

        setUp();

        btnOnClick();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            childUID = currentUser.getUid();
        }

        childUID = OnceAttackRecordStatic.getChildUid();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {

            CurrentTimeView = (TextView)view.findViewById(R.id.assess_guide_time);
            setUp();
            
        }
    }

    public void setUp(){

        //set up showing time
        String timeTmp = OnceAttackRecordStatic.getAttackTimestamp();
        if(timeTmp != null){
            String yyyy = OnceAttackRecordStatic.getAttackTimestampYear();
            String MM = OnceAttackRecordStatic.getAttackTimestampMonth();
            String dd = OnceAttackRecordStatic.getAttackTimestampDay();
            String HH = OnceAttackRecordStatic.getAttackTimestampHour();
            String mm = OnceAttackRecordStatic.getAttackTimestampMinute();
            String currentTime = yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm;
            CurrentTimeView.setText(currentTime);
        }
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

                    //record pk and fev corresponding time
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
//                    int newMargin = currentMargin - 2;
                    OnceAttackRecordStatic.setInhalorMargin(String.valueOf(currentMargin));

                    //record count
                    int currentCount = OnceAttackRecordStatic.getCount();
                    currentCount++;
                    OnceAttackRecordStatic.setCount(currentCount);

                    int childHeight = Integer.valueOf(OnceAttackRecordStatic.getChildHeight());

//                    switch (checkAttack(childHeight, pkNum, fevNum)){
//                        case -1:
//                            enterActivity(FRAG_RED_INDEX);
//                            break;
//                        case 0:
//                            enterActivity(FRAG_YELLOW_INDEX);
//                            break;
//                        case 1:
//                            mActivity.setViewPager(FRAG_GREEN_INDEX);
//                            break;
//                    }

                    Intent intent = new Intent(mActivity, HeatRateDetectActivity.class);
                    startActivity(intent);
                    mActivity.finish();
                }
            }
        });
    }

    private int checkAttack(int height, String pkNum, String fevNum) {

        int flag = -99;

        int pkNumInt = Integer.valueOf(pkNum);
        int fevNumInt = Integer.valueOf(fevNum);

        int attRef = AttackReferenceStatic.attRefMap.get(height);

        //compare to the form and check whether it is a valid attack
        if (pkNumInt < attRef * 0.5) {
            flag = -1;
        } else if (fevNumInt < 60) {
            flag = -1;

        } else if (pkNumInt >= attRef * 0.5 && pkNumInt <= attRef * 0.8) {
            flag = 0;

        } else if (fevNumInt >= 60 && fevNumInt < 80) {
            flag = 0;

        } else {
            flag = 1;
        }

        return flag;
    }

    private void enterActivity(int Flag) {

        mActivity.finish();

        //enter different management process step
        Intent intent;

        if (Flag == FRAG_EMER_INDEX){

            //fragment enter activity
            intent = new Intent(mActivity, EmergencyActivity.class);

        }else{

            intent = new Intent(mActivity, InstructionActivity.class);
            intent.putExtra("pageFlag", String.valueOf(Flag));
        }

        startActivity(intent);
    }

}
