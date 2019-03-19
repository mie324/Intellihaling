package com.project.ece1778_project_intellihaling;

import android.content.Intent;
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
import com.project.ece1778_project_intellihaling.model.AttackReferenceStatic;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import java.util.HashMap;
import java.util.Map;

public class InstructionGuideFragment extends Fragment {

    private static final String TAG = "InstructionGuideFragmen";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;
    private static final int FRAG_YELLOW_INDEX = 4;
    private static final int FRAG_RED_INDEX = 5;


    private static final int ATTACK_COUNT_LIMIT = 5;

    private View view;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String childUID;

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

        view = inflater.inflate(R.layout.fragment_instruction_guide, container, false);

        CurrentTimeView = (TextView)view.findViewById(R.id.instruction_guide_time);

        PKEditText = (EditText)view.findViewById(R.id.instruction_guide_pfnum_enter);
        FEVEditText = (EditText)view.findViewById(R.id.instruction_guide_fevnum_enter);

        confirmBtn = (Button)view.findViewById(R.id.btn_instruction_guide_enter_done);

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

            CurrentTimeView = (TextView)view.findViewById(R.id.instruction_guide_time);
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
                    int newMargin = currentMargin - 2;
                    OnceAttackRecordStatic.setInhalorMargin(String.valueOf(newMargin));

                    //record count
                    int currentCount = OnceAttackRecordStatic.getCount();
                    currentCount++;
                    OnceAttackRecordStatic.setCount(currentCount);

                    int childHeight = Integer.valueOf(OnceAttackRecordStatic.getChildHeight());

                    //if count over 5, should direct to emergency
                    if(currentCount > ATTACK_COUNT_LIMIT){

                        int re = checkAttack(childHeight, pkNum, fevNum);
                        if (re == 1)
                            mActivity.setViewPager(FRAG_GREEN_INDEX);
                        else{

                            //send the record
                            sendAttackRecord();

                            sendInhalerMargin(new FireStoreCallback() {
                                @Override
                                public void onCallback() {
                                    Log.d(TAG, "send inhaler Margin onCallback: " + OnceAttackRecordStatic.getInhalorMargin());

                                    enterEmergencyActivity();
                                }
                            });
                        }
                    }else{
                        switch (checkAttack(childHeight, pkNum, fevNum)){
                            case -1:
                                mActivity.setViewPager(FRAG_RED_INDEX);
                                break;
                            case 0:
                                mActivity.setViewPager(FRAG_YELLOW_INDEX);
                                break;
                            case 1:
                                mActivity.setViewPager(FRAG_GREEN_INDEX);
                                break;
                        }
                    }
                }
            }
        });
    }

    private int checkAttack(int height, String pkNum, String fevNum){

        int flag = -99;

        int pkNumInt = Integer.valueOf(pkNum);
        int fevNumInt = Integer.valueOf(fevNum);

        int attRef = AttackReferenceStatic.attRefMap.get(height);

        //compare to the form and check whether it is a valid attack
        if(pkNumInt < attRef * 0.5 && fevNumInt < 60) {
            flag = -1;

        }else if(pkNumInt >= attRef * 0.5  && pkNumInt <= attRef * 0.8 && fevNumInt >= 60 && fevNumInt < 80){
            flag = 0;

        }else{
            flag = 1;
        }

        return flag;
    }

    private void sendAttackRecord(){

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

    private void enterEmergencyActivity() {

        //fragment enter activity
        Intent intent = new Intent(mActivity, EmergencyActivity.class);
        startActivity(intent);
    }

}
