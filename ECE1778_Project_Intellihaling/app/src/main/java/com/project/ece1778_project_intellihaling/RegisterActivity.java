package com.project.ece1778_project_intellihaling;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int AUTHANTIC_PWD = 6;

    private Bitmap image;

    //UI Inferences
    private View mRegistrationFormView;
    private RadioButton mRbtnParent, mRbtnChild;
    private ImageView userIcon;
    private EditText userName, userEmail, userPwd, userConfirmPwd, childHeight, childWeight;
    private Button SignInButton;
    private Context mContext;

    private String name, email, pwd, confirmPwd, height, weight;

    private ProgressBar registrationProgress;
    private TextView registrationWating;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;

    private String uID;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegistrationFormView=findViewById(R.id.registration_form);

        //info
        userIcon=findViewById(R.id.registration_icon_parent);
        userName=findViewById(R.id.registration_name);
        userEmail=findViewById(R.id.registration_email);
        userPwd=findViewById(R.id.registration_pwd);
        userConfirmPwd=findViewById(R.id.registration_confirmPwd);

        childHeight=findViewById(R.id.registration_height_child);
        childWeight=findViewById(R.id.registration_weight_child);
        childHeight.setVisibility(View.INVISIBLE);
        childWeight.setVisibility(View.INVISIBLE);

        mRbtnParent=(RadioButton) findViewById(R.id.registration_parent_rbtn);
        mRbtnChild=(RadioButton) findViewById(R.id.registration_child_rbtn);

        SignInButton = (Button) findViewById(R.id.btn_signup);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        registrationProgress=findViewById(R.id.registration_progress);
        registrationWating=findViewById(R.id.registration_waiting_text);

        mContext = RegisterActivity.this;

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        showProgress(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            String uID = currentUser.getUid();

            if(isParent(uID))
                role = "parent";
            else
                role = "child";

            enterProfileActivity();
        }else{

        }
    }

    public void setIcon(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            image = (Bitmap) extras.get("data");

            //convert to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageData = stream.toByteArray();

            //compress the photo
            BitmapFactory.Options options = new BitmapFactory.Options();
            //get the info of photo, don't load the photo
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
            int height = options.outHeight;
            int width = options.outWidth;
            //compress ratio
            int inSampleSize = 2;
            int minLen = Math.min(height, width);
            //if photo > 100dp
            if (minLen > 100) {
                float ratio = (float) minLen / 100.0f;
                inSampleSize = (int) ratio;
            }

            //load the photo
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

            //set icon
            userIcon.setImageBitmap(image);

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        if(email.contains("@"))
            return true;
        else
            return false;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > AUTHANTIC_PWD;
    }

    private void attemptSignUp() {

        // Reset errors.
        userName.setError(null);
        userEmail.setError(null);
        userPwd.setError(null);
        userConfirmPwd.setError(null);

        // Store values at the time of the login attempt.
        name = userName.getText().toString();
        email = userEmail.getText().toString();
        pwd = userPwd.getText().toString();
        confirmPwd = userConfirmPwd.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(pwd) && !isPasswordValid(pwd)) {
            userPwd.setError(getString(R.string.error_invalid_password));
            focusView = userPwd;
            cancel = true;
        }

        if(!TextUtils.isEmpty(confirmPwd) && !pwd.equals(confirmPwd)){
            userConfirmPwd.setError(getString(R.string.error_incorrect_confirmPwd));
            focusView = userConfirmPwd;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            userEmail.setError(getString(R.string.error_field_required));
            focusView = userEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            userEmail.setError(getString(R.string.error_invalid_email));
            focusView = userEmail;
            cancel = true;
        }

        // Check for non empty name
        if(TextUtils.isEmpty(name)) {
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }

        if(image == null){
            Toast.makeText(RegisterActivity.this, "photo needed", Toast.LENGTH_SHORT).show();
            focusView = userIcon;
            cancel = true;
        }

        if (!mRbtnChild.isChecked() && !mRbtnParent.isChecked()){
            Toast.makeText(RegisterActivity.this, "need to select role", Toast.LENGTH_SHORT).show();
            focusView = mRbtnParent;
            cancel = true;
        }

        if(role == "child"){
            height = childHeight.getText().toString();
            weight = childWeight.getText().toString();

            // Check for non empty height and weight
            if (TextUtils.isEmpty(height)) {
                childHeight.setError(getString(R.string.error_field_required));
                focusView = childHeight;
                cancel = true;
            }

            if(TextUtils.isEmpty(weight)){
                childWeight.setError(getString(R.string.error_field_required));
                focusView = childWeight;
                cancel = true;
            }
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            showProgress(true);

            FirebaseSignUp();

        }
    }

    private void FirebaseSignUp(){

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "create account:failed", task.getException());
                            showProgress(false);
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();

                        }else {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d(TAG, "register: success. email is verified.");

                            setUp();

                        }
                    }
                });
    }

    private void setUp(){

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", email);
        newUser.put("password", pwd);
        newUser.put("name", name);

        FirebaseUser user = mAuth.getCurrentUser();
        uID = user.getUid();

        //check the role
        if(role == "parent"){

            newUser.put("role", "parent");
            newUser.put("iconPath", uID);
            newUser.put("childsUid", "");

        }else if(role == "child"){

            newUser.put("role", "child");
            newUser.put("parentUid", "");
            newUser.put("iconPath", uID);
            newUser.put("height", "");
            newUser.put("weight", "");
            newUser.put("inhalerId", "");

        }

        mDatabase.collection(role).document(uID)
                .set(newUser)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext,"SignUp Failed",Toast.LENGTH_LONG);
                    }
                });

        //send parent icon to storage
        ByteArrayOutputStream streamImg = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, streamImg);
        //convert to byte array
        byte[] imageData = streamImg.toByteArray();

        // Create a storage reference from app
        StorageReference storageRef = mStorage.getReference();
        // Create a reference to bio picture
        StorageReference bioPictureRef = storageRef.child(uID);
        //upload to storage
        bioPictureRef.putBytes(imageData);

        try {
            streamImg.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sign up successful
        enterProfileActivity();
    }


    private void enterProfileActivity(){

        showProgress(false);
        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.registration_parent_rbtn:
                if (checked){
                    mRbtnChild.setChecked(false);
                    role = "parent";
                    childHeight.setVisibility(View.INVISIBLE);
                    childWeight.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.registration_child_rbtn:
                if (checked){
                    mRbtnParent.setChecked(false);
                    role = "child";
                    childHeight.setVisibility(View.VISIBLE);
                    childWeight.setVisibility(View.VISIBLE);
                }
                    break;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegistrationFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            registrationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            registrationProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registrationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            registrationWating.setVisibility(show ? View.VISIBLE : View.GONE);
            registrationWating.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registrationWating.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            registrationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            registrationWating.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
