package com.project.ece1778_project_intellihaling;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

public class InstructionResultFailFragment extends Fragment {

    private static final String TAG = "InstructionResultFailFr";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_PASS_INDEX = 2;
    private static final int FRAG_FAIL_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;

    private static final int COUNT_NUMBER = 5;

    private int count_num = COUNT_NUMBER, count_final = 0;
    private boolean flag = true;

    //UI
    private TextView CurrentTimeView;
    private TextView inhaleCount;
    private Button startBtn;

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
        View view = inflater.inflate(R.layout.fragment_instruction_fail, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_time);
        inhaleCount = (TextView)view.findViewById(R.id.instruction_fail_text_count_view);
        startBtn = (Button)view.findViewById(R.id.btn_instruction_fail_time_count);

        setUp();

        startCounting();

        return view;
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
                if(str.equals("start")){

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
                                    startBtn.setText("done");
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

                }else if(str.equals("done")){
                    mActivity.setViewPager(FRAG_GUIDE_INDEX);
                }
            }
        });
    }
}
