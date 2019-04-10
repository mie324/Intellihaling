package com.project.ece1778_project_intellihaling;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

public class InstructionResultYellowFragment extends Fragment {

    private static final String TAG = "InstructionResultYellow";
    private static final String INHALER_REMINDER = "inhalerDecreaseReminder";

    private static final int FRAG_GUIDE_INDEX = 0;
    private static final int FRAG_GREEN_INDEX = 1;
    private static final int FRAG_YELLOW_INDEX = 2;
    private static final int FRAG_RED_INDEX = 3;

    private static final int COUNT_NUMBER = 3;

    private int count_num = COUNT_NUMBER, count_final = 0;
    private boolean flag = true;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String uID;

    //UI
    private TextView CurrentTimeView;
    private TextView inhaleCount;
    private Button startBtn;

    //vars
    private InstructionActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (InstructionActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction_yellow, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_yellow_time);
        inhaleCount = (TextView)view.findViewById(R.id.instruction_yellow_text_count_view);
        startBtn = (Button)view.findViewById(R.id.btn_instruction_yellow_time_count);

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        setUp();

        startCounting();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            uID = currentUser.getUid();
        }
    }

    private void setUp(){

        String yyyy = OnceAttackRecordStatic.getAttackTimestampYear();
        String MM = OnceAttackRecordStatic.getAttackTimestampMonth();
        String dd = OnceAttackRecordStatic.getAttackTimestampDay();
        String HH = OnceAttackRecordStatic.getAttackTimestampHour();
        String mm = OnceAttackRecordStatic.getAttackTimestampMinute();
        String currentTime = "Start: " + yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm;
        CurrentTimeView.setText(currentTime);

        //set up inhale count
        int currentCount = OnceAttackRecordStatic.getCount();
        String countText = "#" + String.valueOf(currentCount);
        inhaleCount.setText(countText);

    }

    public void startCounting(){

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String str = startBtn.getText().toString();
                if(!str.equals("Done")){

                    //init
                    flag = true;
                    count_num = COUNT_NUMBER;
                    startBtn.setClickable(false);

                    new Thread( new Runnable() {
                        public void run() {
                            while( flag) {
                                if(count_num != count_final ){
                                    try {
                                        handle.sendMessage( handle.obtainMessage());
                                        Thread.sleep(1000);
                                    } catch( Throwable t) {

                                    }
                                }else{
                                    flag = false;

                                    startBtn.setClickable(true);
                                    startBtn.setText("Done");
                                }
                            }
                        }

                        Handler handle = new Handler() {
                            public void handleMessage( Message msg) {
                                startBtn.setText( ""+ count_num );
                                count_num--;
                            }
                        };
                    }).start();

                }else{

                    //send notification
                    mDatabase.collection(INHALER_REMINDER).document(uID).update("flag",String.valueOf(System.currentTimeMillis()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "onFailure: failure", e);
                                }
                            });

                    mActivity.setViewPager(FRAG_GUIDE_INDEX);
                }
            }
        });
    }
}
