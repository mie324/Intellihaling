package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private String uID;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mContext = ProfileActivity.this;

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            uID = currentUser.getUid();

        }else{
            enterLoginActivity();
        }
    }

    private void enterLoginActivity(){
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
