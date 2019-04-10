package com.project.ece1778_project_intellihaling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class AssessHeartRateFragment extends Fragment{

    private static final String TAG = "InstructionHeartRateFra";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;
    private static final int FRAG_YELLOW_INDEX = 4;
    private static final int FRAG_RED_INDEX = 5;

    //UI
    private TextView CurrentTimeView;
    private TextView hrDisplay;

    //vars
    private AssessActivity mActivity;

    //Heart rate
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (AssessActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assess_heart_rate, container, false);

//        mOpenCVCameraView = (HeartBeatCameraView) view.findViewById(R.id.OpenCVCameraView);
//        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
//        mOpenCVCameraView.setMaxFrameSize(200, 200);
//        mOpenCVCameraView.setCvCameraViewListener(this);
//
        CurrentTimeView = (TextView)view.findViewById(R.id.assess_em_time);
//        hrDisplay = (TextView)view.findViewById(R.id.hrDisplay);

        setUp();

        return view;
    }

//    @Override
//    public void onResume()
//    {
//        super.onResume();
//
//        if (!OpenCVLoader.initDebug()) {
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, mActivity, mLoaderCallback);
//        } else {
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }
//
//        // testing for real time thread
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //numSteps.setText("Count: " + stepCounter);
//
//                // we add 100 new entries
//                for (int i = 0; i < 10000000; i++) {
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            addEntry();
//                        }
//                    });
//
//                    // slow down the addition of entries
//                    try {
//                        // approximately 70 milliseconds per frame
//                        Thread.sleep(70);
//
//                    } catch (InterruptedException e) {
//                        // manage error ...
//                    }
//                }
//            }
//        }).start();
//    }
//
//    // add random data to graph
//    private void addEntry() {
//
//        previous_value = current_value;
//        current_value = colorCount;
//
//        pos_velocity = (current_value - previous_value) > 0;
//
//        if ((current_value - previous_value) < 0){
//            // only reset downswing once it has started declining
//            down_swing = true;
//        }
//
//        if (!calibration_second) {
//            if ((current_value > prev_second_avg) && (pos_velocity) && down_swing){
//                peak_count += 1;
//                down_swing = false;
//
//                System.out.println(peak_count);
//            }
//        }
//
//        prev_second[sub_second_counter] = current_value;
//
//        // approximating 15 frames per second
//        sub_second_counter += 1;
//        if (sub_second_counter == 15){
//            if (calibration_second){
//                calibration_second = false;
//            }
//            prev_second_avg = 0;
//
//            // 1 second has passed, approximate the heartbeat
//            for(int k = 0; k < 15; k++){
//                prev_second_avg += prev_second[k];
//            }
//
//            prev_second_avg = prev_second_avg / 15;
//
//            sub_second_counter = 0;
//            second_counter += 1;
//
//            if (second_counter == 10){
//                hr_num = (peak_count * 6);
//
//                System.out.println("********************printing rate*********************************");
//                System.out.println(peak_count * 6);
//                hrDisplay.setText("Heart Rate: " + hr_num);
//                System.out.println("********************printing rate*********************************");
//                peak_count = 0;
//                second_counter = 0;
//            }
//
//            else{
//                if (current_value > 6500000){
//                    hrDisplay.setText("Heart Rate: " + hr_num + " Place finger in front of camera, have magnitude within range");
//                } else {
//                    hrDisplay.setText("Heart Rate: " + hr_num + " ( Calibrating: " + (5 - second_counter) + " more seconds )");
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onPause()
//    {
//        super.onPause();
////        mOpenCVCameraView.turnOffFlashlight();
//        flashlight_off = true;
//    }
//
//    @Override
//    public void onDestroy()
//    {
//        super.onDestroy();
//        if (mOpenCVCameraView != null)
//            mOpenCVCameraView.disableView();
//    }
//
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat currentFrame = inputFrame.rgba();
//
//        if (flashlight_off) {
//            mOpenCVCameraView.turnOnFlashlight();
//        }
//
//
//        currentFrame.reshape(0, 1);
//        Mat ones = Mat.ones(currentFrame.size(), currentFrame.type());
//        int numOnes = (int)currentFrame.dot(ones);
//
//        colorCount = numOnes;
//
//        // if colorCount is outside of our expected range:
//        // probably an outlier, do not sync to it
//        if ((colorCount > 5800000) || (colorCount < 5300000)){
//            calibration_second = true;
//            sub_second_counter = 0;
//            second_counter = 0;
//        }
//
//        return currentFrame;
//    }
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//    }
//
//    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mActivity) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                {
//                    Log.i(TAG, "OpenCV loaded successfully");
//                    mOpenCVCameraView.enableView();
//                } break;
//                default:
//                {
//                    super.onManagerConnected(status);
//                } break;
//            }
//        }
//    };

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
