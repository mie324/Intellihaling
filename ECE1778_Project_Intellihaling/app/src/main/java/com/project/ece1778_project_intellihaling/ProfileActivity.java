package com.project.ece1778_project_intellihaling;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.project.ece1778_project_intellihaling.model.Child;
import com.project.ece1778_project_intellihaling.model.Parent;
import com.project.ece1778_project_intellihaling.util.BottomNavigationViewHelper;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private static final int ACTIVITY_NUM = 2;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private String uID;

    private Context mContext;

    //widgets
    private ImageView profileIcon;
    private TextView profileName, profileRole;
    private Button editBtn;

    //object
    String role;
    Parent parentInfo;
    Child childInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mContext = ProfileActivity.this;

        profileIcon = findViewById(R.id.profile_photo);
        profileName = findViewById(R.id.profile_username);
        profileRole = findViewById(R.id.profile_role);

        editBtn=findViewById(R.id.profile_edit);

        //setting firebase, get UID
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        if(intent.hasExtra("role"))
            role = intent.getStringExtra("role");
        else
            role = "";

        setupBottomNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uID = currentUser.getUid();

        if(role == ""){
            if(isParent(uID))
                role = "parent";
            else
                role = "child";
        }

        switch (role){
            case "parent":
                retriveParentInfo();
                break;

            case "child":
                retriveChildInfo();
                break;

            case "":
                enterLoginActivity();
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        finish();
        Intent intent = null;
        if(uID == ""){
            intent = new Intent(ProfileActivity.this, MainActivity.class);
        }
        else
            intent = getIntent();

        startActivity(intent);
    }

    private void retriveParentInfo() {

        DocumentReference docRef = mDatabase.collection("parent").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete( Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        parentInfo = new Parent(uID,  (String)document.get("email"), (String)document.get("name")
                                ,(String)document.get("password"), (String)document.get("iconPath"), (String)document.get("childsUid"));

                        parentProfileSetUp();

                    } else {
                        Log.d(TAG, "get parent info onComplete: document not exists");
                    }
                } else {
                    Log.d(TAG, "get parent info onComplete: task fails");
                }
            }
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void retriveChildInfo() {

        final DocumentReference docRef = mDatabase.collection("child").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        childInfo = new Child(uID, (String)document.get("parentUid"),(String)document.get("email")
                                , (String)document.get("password"), (String)document.get("name"));

                        String h = (String)document.get("height");
                        String w = (String)document.get("weight");
                        String iP = (String)document.get("iconPath");
                        String ih = (String)document.get("inhalerId");

                        if (h != "")
                            childInfo.setHeight(h);
                        if (w != "")
                            childInfo.setWeight(w);
                        if(iP != "")
                            childInfo.setIconPath(iP);
                        if(ih != "")
                            childInfo.setInhalerId(ih);

                        childProfileSetup();

                    } else {
                        Log.d(TAG, "get child info onComplete: document not exists");
                    }
                } else {
                    Log.d(TAG, "get child info onComplete: task fails");
                }
            }
        });

    }

    private void parentProfileSetUp(){

        profileName.setText(parentInfo.getName());
        profileRole.setText(role);

        //set the corresponding bio photo
        StorageReference stRef = mStorage.getReference();
        StorageReference bioPhoto = stRef.child(parentInfo.getUid());
        final long ONE_MEGABYTE = 1024 * 1024;
        //download to rom
        bioPhoto.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileIcon.setImageBitmap(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        //check parent already link a child account
        if(parentInfo.getChildsUid() == ""){
            editBtn.setText("Link account with a child");
        }else{
            editBtn.setText("Edit Information");
        }
    }

    private void childProfileSetup(){

        profileName.setText(childInfo.getName());
        profileRole.setText(role);

        //set the corresponding bio photo
        StorageReference stRef = mStorage.getReference();
        StorageReference bioPhoto = stRef.child(childInfo.getUid());
        final long ONE_MEGABYTE = 1024 * 1024;
        //download to rom
        bioPhoto.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileIcon.setImageBitmap(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        editBtn.setText("Set up your health information!");
    }

    public void editInfo(View view){

        String str = editBtn.getText().toString();

        switch (str){

            case "Link account with a child":
                Intent intent = new Intent(ProfileActivity.this, LinkChildActivity.class);
                startActivity(intent);
                finish();
                break;

            case "Edit Information":
                break;

            case "Set up your health information!":
                break;
        }


    }

    private boolean isParent(String uid) {
        DocumentReference docRef = mDatabase.collection("parent").document(uid);
        Log.d(TAG, "isParent: check if docRef is null");
        if (docRef == null) {
            return false;
        } else {
            return true;
        }
    }

    private void enterLoginActivity() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //log out
    public void logout(View view){
        mAuth.signOut();
        Toast.makeText(mContext, "Log Out Success", Toast.LENGTH_LONG);
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        this.finish();
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
