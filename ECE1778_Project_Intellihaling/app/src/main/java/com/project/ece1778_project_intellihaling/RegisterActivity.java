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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int AUTHANTIC_PWD = 6;

    private Bitmap image;

    //UI Inferences
    private View mRegistrationFormView;
    private ImageView userParentIcon, userChildIcon;
    private String parentName, parentEmail, parentPwd, parentConfirmPwd;
    private String childName, childEmail;
    private EditText userParentName, userParentEmail, userParentPwd, userParentConfirmPwd;
    private EditText userChildName, userChildEmail;
    private Button SignInButton;
    private Context mContext;

    private ProgressBar registrationProgress;
    private TextView registrationWating;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegistrationFormView=findViewById(R.id.registration_form);

        //parent info
        userParentIcon=findViewById(R.id.registration_icon_parent);
        userParentName=findViewById(R.id.registration_name_parent);
        userParentEmail=findViewById(R.id.registration_email_parent);
        userParentPwd=findViewById(R.id.registration_pwd_parent);
        userParentConfirmPwd=findViewById(R.id.registration_confirmPwd_parent);

        //child info
        userChildIcon=findViewById(R.id.registration_icon_child);
        userChildName=findViewById(R.id.registration_name_child);
        userChildEmail=findViewById(R.id.registration_email_child);


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
            enterProfileActivity();
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
        userParentName.setError(null);
        userParentEmail.setError(null);
        userParentPwd.setError(null);
        userParentConfirmPwd.setError(null);

        userChildName.setError(null);
        userChildEmail.setError(null);

        // Store values at the time of the login attempt.
        parentName = userParentName.getText().toString();
        parentEmail = userParentEmail.getText().toString();
        parentPwd = userParentPwd.getText().toString();
        parentConfirmPwd = userParentConfirmPwd.getText().toString();

        childName = userChildName.getText().toString();
        childEmail = userChildEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(parentPwd) && !isPasswordValid(parentPwd)) {
            userParentPwd.setError(getString(R.string.error_invalid_password));
            focusView = userParentPwd;
            cancel = true;
        }

        if(!TextUtils.isEmpty(parentConfirmPwd) && !parentPwd.equals(parentConfirmPwd)){
            userParentConfirmPwd.setError(getString(R.string.error_incorrect_confirmPwd));
            focusView = userParentConfirmPwd;
            cancel = true;
        }

        // parentCheck for a valid email address.
        if (TextUtils.isEmpty(parentEmail)) {
            userParentEmail.setError(getString(R.string.error_field_required));
            focusView = userParentEmail;
            cancel = true;
        } else if (!isEmailValid(parentEmail)) {
            userParentEmail.setError(getString(R.string.error_invalid_email));
            focusView = userParentEmail;
            cancel = true;
        }

        // Check for non empty name
        if(TextUtils.isEmpty(parentName)) {
            userParentName.setError(getString(R.string.error_field_required));
            focusView = userParentName;
            cancel = true;
        }

        //child
        // Check for a valid email address.
        if (TextUtils.isEmpty(childEmail)) {
            userChildEmail.setError(getString(R.string.error_field_required));
            focusView = userChildEmail;
            cancel = true;
        } else if (!isEmailValid(childEmail)) {
            userChildEmail.setError(getString(R.string.error_invalid_email));
            focusView = userChildEmail;
            cancel = true;
        }

        // Check for non empty name
        if(TextUtils.isEmpty(childName)) {
            userChildName.setError(getString(R.string.error_field_required));
            focusView = userChildName;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            showProgress(true);
            SignUp();
        }
    }

    private void SignUp(){

    }


    private void enterProfileActivity(){

        //too fast to ask data from firebase will fail, wait a little bit
        try {
            // Simulate network access.
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        showProgress(false);
        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
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
