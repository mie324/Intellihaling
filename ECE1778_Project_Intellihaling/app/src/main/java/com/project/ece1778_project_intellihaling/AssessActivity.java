package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.project.ece1778_project_intellihaling.util.MyFragmentPagerAdapter;

public class AssessActivity extends AppCompatActivity {

    private static final String TAG = "AssessActivity";

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_HEART_INDEX = 2;
    private static final int FRAG_GREEN_INDEX = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private DocumentReference docRef;
    private FirebaseStorage mStorage;
    private String uID;

    private Context mContext;

    //fragment
    private ViewPager mViewPager;
    private AssessMainFragment AssMainFr;
    private AssessHeartRateFragment AssHeartFr;
    private AssessGuideFragment AssGuideFr;
    private AssessResultGreenFragment AssGreenFr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assess);

        mContext = AssessActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            uID = currentUser.getUid();

        }else{
            enterMainActivity();
        }

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == FRAG_MAIN_INDEX) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            this.finish();
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        finish();
        Intent intent = null;
        if(uID == ""){
            intent = new Intent(AssessActivity.this, MainActivity.class);
            finish();
        }
        else
            intent = getIntent();

        startActivity(intent);
    }

    private void init(){

        AssMainFr = new AssessMainFragment();
        AssGuideFr = new AssessGuideFragment();
        AssHeartFr = new AssessHeartRateFragment();
        AssGreenFr = new AssessResultGreenFragment();

        mViewPager = (ViewPager)findViewById(R.id.assess_container);
        //set up pager
        setupViewPager(mViewPager);

        mViewPager.setCurrentItem(FRAG_MAIN_INDEX);

    }

    private void setupViewPager(ViewPager viewPager){
        MyFragmentPagerAdapter adpter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        adpter.addFragment(AssMainFr); //index 0

        adpter.addFragment(AssGuideFr); //index 1
//        adpter.addFragment(AssHeartFr); //index 2
//        adpter.addFragment(AssGreenFr);

        viewPager.setAdapter(adpter);

    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void enterMainActivity(){

        Intent intent = new Intent(AssessActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
