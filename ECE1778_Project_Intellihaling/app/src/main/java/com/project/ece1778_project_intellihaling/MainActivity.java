package com.project.ece1778_project_intellihaling;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btn_enter;
    private static final int REQUEST_LOGIN = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private DocumentReference docRef;
    private String uID;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        init();
    }
    private void init()
    {
        btn_enter = (Button)this.findViewById(R.id.btn_mainEnter);
        btn_enter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Check if user is signed in (non-null)
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(currentUser != null) {
                    uID = currentUser.getUid();

                    docRef = mDatabase.collection("parent").document(uID);
                    detectRole(new FireStoreCallback() {
                        @Override
                        public void onCallback(String role) {
                            Log.d(TAG, "onCallback: " + role);
                            enterProfileActivity();
                        }
                    });

                }else{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, REQUEST_LOGIN);
                }
            }
        });
    }

    private void detectRole(final FireStoreCallback fireStoreCallback){

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        role = "parent";
                    }else{
                        role = "child";
                    }

                    fireStoreCallback.onCallback(role);

                }else {
                    Log.d(TAG, "detect role onComplete: task fails: ", task.getException() );
                }
            }
        });
    }

    private interface FireStoreCallback{
        void onCallback(String role);
    }

    private void enterProfileActivity(){

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement signup logic
                // Default
                this.finish();
            }
        }
    }
}
