package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.ece1778_project_intellihaling.model.Child;
import com.project.ece1778_project_intellihaling.util.MyFragmentPagerAdapter;

public class InstructionActivity extends AppCompatActivity {

    private static final String TAG = "ManagementActivity";

    private static final int FRAG_GUIDE_INDEX = 0;
    private static final int FRAG_GREEN_INDEX = 1;
    private static final int FRAG_YELLOW_INDEX = 2;
    private static final int FRAG_RED_INDEX = 3;

    //firebase
    private FirebaseAuth mAuth;;
    private String uID;

    private Context mContext;

    //object
    private Child childInfo;

    //fragment
    private ViewPager mViewPager;
//    private InstructionMainFragment InsMainFrag;
//    private InstructionHeartRateFragment InsHeartFrag;
    private InstructionGuideFragment InsGuideFrag;
    private InstructionResultGreenFragment InsGreenFrag;
    private InstructionResultYellowFragment InsYellowFrag;
    private InstructionResultRedFragment InsRedFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        mContext = InstructionActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();

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
//        if (mViewPager.getCurrentItem() == FRAG_GUIDE_INDEX) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            this.finish();
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
//        }

        this.finish();
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        finish();
        Intent intent = null;
        if(uID == ""){
            intent = new Intent(InstructionActivity.this, MainActivity.class);
            finish();
        }
        else
            intent = getIntent();

        startActivity(intent);
    }

    private void init(){

//        InsMainFrag = new InstructionMainFragment();
        InsGuideFrag = new InstructionGuideFragment();
//        InsHeartFrag = new InstructionHeartRateFragment();
        InsGreenFrag = new InstructionResultGreenFragment();
        InsYellowFrag = new InstructionResultYellowFragment();
        InsRedFrag = new InstructionResultRedFragment();

        mViewPager = (ViewPager)findViewById(R.id.management_container);
        //set up pager
        setupViewPager(mViewPager);

        Intent intent = getIntent();
        String pageFlag = intent.getStringExtra("pageFlag");

        if(!intent.hasExtra("pageFlag")){
            mViewPager.setCurrentItem(FRAG_GUIDE_INDEX);

        }else{
            switch (pageFlag){
                case "1":
                    mViewPager.setCurrentItem(FRAG_GREEN_INDEX);
                    break;
                case "2":
                    mViewPager.setCurrentItem(FRAG_YELLOW_INDEX);
                    break;
                case "3":
                    mViewPager.setCurrentItem(FRAG_RED_INDEX);
                    break;
            }
        }

    }

    private void setupViewPager(ViewPager viewPager){
        MyFragmentPagerAdapter adpter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        adpter.addFragment(InsGuideFrag); //index 0

        adpter.addFragment(InsGreenFrag); //index 1
        adpter.addFragment(InsYellowFrag); //index 2
        adpter.addFragment(InsRedFrag); //index 3

        viewPager.setAdapter(adpter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void enterMainActivity(){

        Intent intent = new Intent(InstructionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
