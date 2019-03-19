package com.project.ece1778_project_intellihaling;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import java.util.HashMap;
import java.util.Map;

public class InstructionResultGreenFragment extends Fragment {

    private static final String TAG = "InstructionResultPassFr";
    private static final String FINE_REMINDER = "fineReminder";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;
    private static final int FRAG_YELLOW_INDEX = 4;
    private static final int FRAG_RED_INDEX = 5;

    private View view;

    //UI
    private TextView CurrentTimeView;
    private Button confirmBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String childUID;

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
        view = inflater.inflate(R.layout.fragment_instruction_green, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_green_time);
        confirmBtn = (Button)view.findViewById(R.id.btn_instruction_green_done_btn);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        setUp();

        btnSetUp();

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

    public void btnSetUp(){

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String tsTmp = OnceAttackRecordStatic.getAttackTimestamp();
//                if(tsTmp.contains("/")){
//
//                    sendAttackRecord();
//
//                    sendInhalerMargin(new FireStoreCallback() {
//                        @Override
//                        public void onCallback() {
//                            Log.d(TAG, "send inhaler Margin onCallback: " + OnceAttackRecordStatic.getInhalorMargin());
//
//                            mActivity.setViewPager(FRAG_MAIN_INDEX);
//                        }
//                    });
//
//                }else{
//                    mActivity.setViewPager(FRAG_MAIN_INDEX);
//                }

                //send notification
                mDatabase.collection(FINE_REMINDER).document(childUID).update("flag",String.valueOf(System.currentTimeMillis()))
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

                sendAttackRecord();

                sendInhalerMargin(new FireStoreCallback() {
                    @Override
                    public void onCallback() {
                        Log.d(TAG, "send inhaler Margin onCallback: " + OnceAttackRecordStatic.getInhalorMargin());

                        mActivity.setViewPager(FRAG_MAIN_INDEX);
                    }
                });
            }
        });
    }

    private void sendAttackRecord(){

        String tsTmp = OnceAttackRecordStatic.getAttackTimestamp();
        if(tsTmp.contains("/")){}

        //upload to cloud, update attack record
        Map<String, Object> newAttackRecord = new HashMap<>();
        newAttackRecord.put("childUid", childUID);
        newAttackRecord.put("attackTimestamp", OnceAttackRecordStatic.getAttackTimestamp());
        newAttackRecord.put("attackTimestampDay", OnceAttackRecordStatic.getAttackTimestampDay());
        newAttackRecord.put("attackTimestampMonth", OnceAttackRecordStatic.getAttackTimestampMonth());
        newAttackRecord.put("attackTimestampYear", OnceAttackRecordStatic.getAttackTimestampYear());
        newAttackRecord.put("peakflow", OnceAttackRecordStatic.getPeakflow());
        newAttackRecord.put("fev", OnceAttackRecordStatic.getFev());
        newAttackRecord.put("peakflowAndFevTimestamp", OnceAttackRecordStatic.getPeakflowAndFevTimestamp());

        mDatabase.collection("attackRecord").document()
                .set(newAttackRecord)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mActivity,"Upload Info Failed",Toast.LENGTH_LONG);
                    }
                });
    }

    private void sendInhalerMargin(final FireStoreCallback fireStoreCallback){

        //upgrade inhaler margin
        CollectionReference inhalorRef = mDatabase.collection("inhaler");
        inhalorRef.whereEqualTo("childUid", childUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String inhalerUid = document.getId();

                        DocumentReference docRef = mDatabase.collection("inhaler").document(inhalerUid);
                        docRef.update("margin", OnceAttackRecordStatic.getInhalorMargin())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");

                                        fireStoreCallback.onCallback();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating document", e);
                                    }
                                });
                    }
                }else{
                    Log.e(TAG, "CollectionReference task fail.", task.getException());
                }
            }
        });
    }

    private interface FireStoreCallback{
        void onCallback();
    }
}
