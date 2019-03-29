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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.ece1778_project_intellihaling.model.Child;
import com.project.ece1778_project_intellihaling.model.Inhaler;
import com.project.ece1778_project_intellihaling.model.OnceAttackRecordStatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AssessMainFragment extends Fragment {

    private static final String TAG = "AssessMainFragment";
    private static final String ASTHMA_REMINDER = "asthmaReminder";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;

    private static final int FRAG_YELLOW_INDEX = 2;
    private static final int FRAG_RED_INDEX = 3;
    private static final int FRAG_EMER_INDEX = 4;;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private DocumentReference docRef;
    private String uID;

    //UI
    private Button confirmBtn;

    //object
    final String role = "child";
    private Child childInfo;
    private Inhaler inhalorInfo;

    //vars
    View view;
    private AssessActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (AssessActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_assess_main, container, false);

        confirmBtn = (Button)view.findViewById(R.id.btn_assess_main_start);

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        childInfo = new Child();
        inhalorInfo = new Inhaler();

        btnOnClick();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            uID = currentUser.getUid();

            docRef = mDatabase.collection(role).document(uID);
            retriveChildInfo(new FireStoreCallback() {
                @Override
                public void onCallback() {
                    Log.d(TAG, "onCallback: " + childInfo.getEmail() + " p: " + childInfo.getParentUid());
                    Log.d(TAG, "onCallback: info child h: " + childInfo.getHeight());

                    retriveInhalorInfo(new FireStoreCallback() {
                        @Override
                        public void onCallback() {
                            Log.d(TAG, "onCallback: " + inhalorInfo.getMargin());
                        }
                    });
                }
            });

        }
    }

    public void btnOnClick() {

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //send notification
                mDatabase.collection(ASTHMA_REMINDER).document(uID)
                        .update("flag",String.valueOf(System.currentTimeMillis()))
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

                //init attach record file
                OnceAttackRecordStatic.setChildUid(uID);

                OnceAttackRecordStatic.setChildHeight(childInfo.getHeight());

                //set inhaler remain puffs medicine
                OnceAttackRecordStatic.setInhalorMargin(inhalorInfo.getMargin());

                //set start time
                long timeMillis = System.currentTimeMillis();
                String timeStamp = String.valueOf(timeMillis);
                OnceAttackRecordStatic.setAttackTimestamp(timeStamp);

                Date ct = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy");
                String year = df.format(ct);
                df = new SimpleDateFormat("MM");
                String month = df.format(ct);
                df = new SimpleDateFormat("dd");
                String day = df.format(ct);
                df = new SimpleDateFormat("HH");
                String hour = df.format(ct);
                df = new SimpleDateFormat("mm");
                String minute = df.format(ct);
                OnceAttackRecordStatic.setAttackTimestampYear(year);
                OnceAttackRecordStatic.setAttackTimestampMonth(month);
                OnceAttackRecordStatic.setAttackTimestampDay(day);
                OnceAttackRecordStatic.setAttackTimestampHour(hour);
                OnceAttackRecordStatic.setAttackTimestampMinute(minute);

                //init the inhaling count
                OnceAttackRecordStatic.setCount(0);

                //init record
                OnceAttackRecordStatic.setPeakflow(null);
                OnceAttackRecordStatic.setFev(null);
                OnceAttackRecordStatic.setPeakflowAndFevTimestamp(null);

                mActivity.setViewPager(FRAG_GUIDE_INDEX);
            }
        });

    }

    private void retriveChildInfo(final FireStoreCallback fireStoreCallback) {

        docRef = mDatabase.collection("child").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        childInfo.setUid(uID);
                        childInfo.setParentUid((String)document.get("parentUid"));
//                        childInfo.setEmail((String)document.get("email"));
//                        childInfo.setPassword((String)document.get("password"));
                        childInfo.setName((String)document.get("name"));

                        String h = (String)document.get("height");
                        String w = (String)document.get("weight");
//                        String iP = (String)document.get("iconPath");
//                        String ih = (String)document.get("inhalerId");

                        if (h != "")
                            childInfo.setHeight(h);
                        if (w != "")
                            childInfo.setWeight(w);
//                        if(iP != "")
//                            childInfo.setIconPath(iP);
//                        if(ih != "")
//                            childInfo.setInhalerId(ih);

                        fireStoreCallback.onCallback();

                    } else {
                        Log.d(TAG, "get child info onComplete: document not exists");
                    }
                } else {
                    Log.d(TAG, "get child info onComplete: task fails");
                }
            }
        });

    }

    private void retriveInhalorInfo(final FireStoreCallback fireStoreCallback){

        final CollectionReference inhalorRef = mDatabase.collection("inhaler");
        inhalorRef.whereEqualTo("childUid", uID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        inhalorInfo.setChildUid(uID);

                        String margin = (String)document.getString("margin");
                        if(margin != "")
                            inhalorInfo.setMargin(margin);

                        fireStoreCallback.onCallback();
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
