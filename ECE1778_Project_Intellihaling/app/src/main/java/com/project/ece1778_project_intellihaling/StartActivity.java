package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.project.ece1778_project_intellihaling.model.Child;
import com.project.ece1778_project_intellihaling.util.BottomNavigationViewHelper;
import com.project.ece1778_project_intellihaling.util.MyFragmentPagerAdapter;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";

    private static final int ACTIVITY_NUM = 1;

    private static final int FRAG_MAIN_INDEX = 0;
    private static final int FRAG_GUIDE_INDEX = 1;
    private static final int FRAG_PASS_INDEX = 2;
    private static final int FRAG_FAIL_INDEX = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private DocumentReference docRef;
    private FirebaseStorage mStorage;
    private String uID;

    private Context mContext;

    //object
    private Child childInfo;

    //fragment
    private ViewPager mViewPager;
    private InstructionMainFragment InsMainFrag;
    private InstructionGuideFragment InsGuideFrag;
    private InstructionResultPassFragment InsPassFrag;
    private InstructionResultFailFragment InsFailFrag;
    private InstructionEmergencyFragment InsEmerFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mContext = StartActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        init();

        setupBottomNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            uID = currentUser.getUid();

        }else{
            enterLoginActivity();
        }

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
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
            intent = new Intent(StartActivity.this, MainActivity.class);
            finish();
        }
        else
            intent = getIntent();

        startActivity(intent);
    }

    private void init(){

        InsMainFrag = new InstructionMainFragment();
        InsGuideFrag = new InstructionGuideFragment();
        InsPassFrag = new InstructionResultPassFragment();
        InsFailFrag = new InstructionResultFailFragment();
        InsEmerFrag = new InstructionEmergencyFragment();

        mViewPager = (ViewPager)findViewById(R.id.start_container);
        //set up pager
        setupViewPager(mViewPager);
        mViewPager.setCurrentItem(FRAG_MAIN_INDEX);

    }

    private void setupViewPager(ViewPager viewPager){
        MyFragmentPagerAdapter adpter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        adpter.addFragment(InsMainFrag); //index 0
        adpter.addFragment(InsGuideFrag); //index 1
        adpter.addFragment(InsPassFrag); //index 2
        adpter.addFragment(InsFailFrag); //index 3
        adpter.addFragment(InsEmerFrag); //index 4

        viewPager.setAdapter(adpter);

    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void enterLoginActivity(){
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.layoutbottomNavBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
