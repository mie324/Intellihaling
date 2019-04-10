package com.project.ece1778_project_intellihaling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import com.project.ece1778_project_intellihaling.model.AttackReferenceStatic;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class HeatRateDetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "HeatRateDetect";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;

    private static final int FRAG_GREEN_INDEX = 1;
    private static final int FRAG_YELLOW_INDEX = 2;
    private static final int FRAG_RED_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;

    //UI
    private TextView CurrentTimeView;
    private TextView hrDisplay;

    //Heart rate
    private static final int COUNT_DOWN_SEC = 5;
    private HeartBeatCameraView mOpenCVCameraView;

    private int hr_num = 0;
    private int previous_value = 0;
    private int current_value = 0;
    private boolean pos_velocity = false;
    private boolean down_swing = false;
    private int peak_count = 0;
    private int prev_second_avg = 0;
    private int[] prev_second = new int[15];

    // are we on the first second's worth of data
    private boolean calibration_second = true;
    private boolean flashlight_off = true;

    private int second_counter = 0;
    private int sub_second_counter = 0;

    private int colorCount = 0;

    private boolean runFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_rate_detect);

        mOpenCVCameraView = (HeartBeatCameraView) findViewById(R.id.OpenCV_CameraView);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setMaxFrameSize(200, 200);
        mOpenCVCameraView.setCvCameraViewListener(this);

        hrDisplay = (TextView)findViewById(R.id.hr_Display);

        CurrentTimeView = (TextView)findViewById(R.id.heart_rate_time);

        runFlag = true;

        setUp();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


        runFlag = true;

        // testing for real time thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // we add 100 new entries
                for (int i = 0; i < 10000000; i++) {

                    if(runFlag == false){

                        try {
                            // approximately 50 milliseconds per frame
                            Thread.sleep(2000);

                        } catch (InterruptedException e) {
                            // manage error ...
                        }

                        assess();
                        break;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // slow down the addition of entries
                    try {
                        // approximately 50 milliseconds per frame
                        Thread.sleep(50);

                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }

    // add random data to graph
    private void addEntry() {

        previous_value = current_value;
        current_value = colorCount;

        pos_velocity = (current_value - previous_value) > 0;

        if ((current_value - previous_value) < 0){
            // only reset downswing once it has started declining
            down_swing = true;
        }

        if (!calibration_second) {
            if ((current_value > prev_second_avg) && (pos_velocity) && down_swing){
                peak_count += 1;
                down_swing = false;

                System.out.println(peak_count);
            }
        }

        prev_second[sub_second_counter] = current_value;

        // approximating 15 frames per second
        sub_second_counter += 1;
        if (sub_second_counter == 15){
            if (calibration_second){
                calibration_second = false;
            }
            prev_second_avg = 0;

            // 1 second has passed, approximate the heartbeat
            for(int k = 0; k < 15; k++){
                prev_second_avg += prev_second[k];
            }

            prev_second_avg = prev_second_avg / 15;

            sub_second_counter = 0;
            second_counter += 1;

            if (second_counter == COUNT_DOWN_SEC){
                hr_num = peak_count * 6;

                System.out.println("********************printing rate*********************************");
                System.out.println(hr_num);
                hrDisplay.setText("Heart Rate: " + hr_num);
                System.out.println("********************printing rate*********************************");
                peak_count = 0;
                second_counter = 0;

                runFlag = false;
            }

            else{
                System.out.println("*********Else printing rate************");

                hr_num = (peak_count * 6);
                if (current_value > 6500000){
                    System.out.println(hr_num);
                    hrDisplay.setText("Heart Rate: " + hr_num + " Place finger in front of camera, have magnitude within range");
                } else {
                    System.out.println(hr_num);
                    hrDisplay.setText("Heart Rate: " + hr_num + " ( Calibrating: " + (COUNT_DOWN_SEC - second_counter) + " more seconds )");
                }

                System.out.println("*********Else printing rate************");
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mOpenCVCameraView.turnOffFlashlight();
        flashlight_off = true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCVCameraView != null)
            mOpenCVCameraView.disableView();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat currentFrame = inputFrame.rgba();

        int cannyThreshold=5;

        if (flashlight_off) {
            mOpenCVCameraView.turnOnFlashlight();
        }

        currentFrame.reshape(0, 1);
        Mat ones = Mat.ones(currentFrame.size(), currentFrame.type());
        int numOnes = (int)currentFrame.dot(ones);

        colorCount = numOnes;
        System.out.print("\nMat colorCount: " + colorCount);

        // if colorCount is outside of our expected range:
        // probably an outlier, do not sync to it
        if ((colorCount > 5900000) || (colorCount < 5200000)){
            calibration_second = true;
            sub_second_counter = 0;
            second_counter = 0;
        }

        return currentFrame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCVCameraView.enableView();
                } break;

                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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

    private void assess(){

        int childHeight = Integer.valueOf(OnceAttackRecordStatic.getChildHeight());
        String pkNum = OnceAttackRecordStatic.getPeakflow();
        String fevNum = OnceAttackRecordStatic.getFev();

        switch (checkAttack(childHeight, pkNum, fevNum)){
            case -1:
                enterActivity(FRAG_RED_INDEX);
                break;
            case 0:
                enterActivity(FRAG_YELLOW_INDEX);
                break;
            case 1:
                enterActivity(FRAG_GREEN_INDEX);
//                mActivity.setViewPager(FRAG_GREEN_INDEX);
                break;
        }
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

//        this.finish();

        //enter different management process step
        Intent intent;

        if (Flag == FRAG_EMER_INDEX){

            //fragment enter activity
            intent = new Intent(this, EmergencyActivity.class);

        }else{

            intent = new Intent(this, InstructionActivity.class);
            intent.putExtra("pageFlag", String.valueOf(Flag));
        }

        startActivity(intent);
        finish();
    }

}
