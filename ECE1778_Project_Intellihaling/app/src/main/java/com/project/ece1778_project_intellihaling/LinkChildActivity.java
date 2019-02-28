package com.project.ece1778_project_intellihaling;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LinkChildActivity extends AppCompatActivity {

    private static final String TAG = "LinkChildActivity";

    //widgets
    private EditText childEmail;
    private Button confirmBtn;

    private String email;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    String parentUID, childUID;

    String role = "parent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_child);

        childEmail=findViewById(R.id.link_child_email);
        confirmBtn=findViewById(R.id.btn_link_child);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            parentUID = currentUser.getUid();
        }else {
            Log.d(TAG, "onStart: cannot find uid");
        }
    }

    public void linkConfirm(View view){

        childEmail.setError(null);

        email = childEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            childEmail.setError(getString(R.string.error_field_required));
            focusView = childEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            childEmail.setError(getString(R.string.error_invalid_email));
            focusView = childEmail;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else{

            linkChild();
        }
    }

    private void linkChild(){

        //use child email to find child uid
        final CollectionReference childRef = mDatabase.collection("child");
        childRef.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        childUID = document.getId();
                    }

                    if (childUID != null){
                        //update parentUID in child file
                        DocumentReference docRef1 = mDatabase.collection("child").document(childUID);
                        docRef1.update("parentUid", parentUID);

                        //update childUID in parent file
                        DocumentReference docRef2 = mDatabase.collection(role).document(parentUID);
                        docRef2.update("childsUid", childUID);

                        enterProfileActivity();

                    }else{
                        // child email not exist, got register!
                        Toast.makeText(LinkChildActivity.this, "Child account not exists, please register child first", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "CollectionReference task fail.", task.getException());

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        enterProfileActivity();
                    }

                }else{
                    Log.e(TAG, "CollectionReference task fail.", task.getException());
                }
            }
        });
    }

    private void enterRegisterActivity(){

        Intent intent = new Intent(LinkChildActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void enterProfileActivity(){

        Intent intent = new Intent(LinkChildActivity.this, ProfileActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        if(email.contains("@"))
            return true;
        else
            return false;
    }
}
