package com.project.ece1778_project_intellihaling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.ece1778_project_intellihaling.model.Inhaler;
import com.project.ece1778_project_intellihaling.model.InhalerManager;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String ASTHMA_REMINDER = "asthmaReminder";
    private static final String INHALER_REMINDER = "inhalerDecreaseReminder";
    private static final String FINE_REMINDER = "fineReminder";

    private static final int MARGIN_MIN = 20;
    private static final int REMAINDAYS_MIN = 5;

    private Button btn_enter;
    private static final int REQUEST_LOGIN = 0;
    private TextView remainingPuffs;
    private TextView remainingDays;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private String uID;
    private String childUID;
    private String role;

    private boolean snapShotFlag = false;

    private Inhaler mInhaler;
    private TextView mMargin;
    private TextView mRemainingDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //widgets
        remainingDays = findViewById(R.id.main_expire_date_textView);
        remainingPuffs = findViewById(R.id.main_puff_textView);
        mMargin = findViewById(R.id.main_puff_textView);
        mRemainingDays = findViewById(R.id.main_expire_date_textView);

        btn_enter = (Button) this.findViewById(R.id.btn_mainEnter);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onStart(){
        super.onStart();

        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uID = currentUser.getUid();

            detectRole(new FireStoreCallback() {
                @Override
                public void onCallback() {
                    Log.d(TAG, "on Start Callback: Successful" );

                    //query inhaler
                    getInhalerInfo(new InhalerInfoSetInterface() {
                        @Override
                        public void inhalerSetter(Inhaler inhaler) {
                            mInhaler = inhaler;
                            inhalerLayout(mInhaler);

                            if (role.equals("parent")) {
                                receiveNotification(ASTHMA_REMINDER);
                                receiveNotification(INHALER_REMINDER);
                                receiveNotification(FINE_REMINDER);
                            }
                        }
                    });
                }
            });

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        snapShotFlag = false;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        finish();
    }

    private void detectRole(final FireStoreCallback fireStoreCallback) {

        DocumentReference docRef = mDatabase.collection("parent").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        role = "parent";
                        childUID = (String) document.get("childsUid");
                    } else {
                        role = "child";
                        childUID = uID;
                    }

                    fireStoreCallback.onCallback();

                } else {
                    Log.d(TAG, "detect role onComplete: task fails: ", task.getException());
                }
            }
        });
    }

    private void inhalerLayout (Inhaler inhaler){

        InhalerManager inhalerManager = new InhalerManager(inhaler);
        String remainingDays = inhalerManager.calculateRemainingDays();
        String margin = inhalerManager.getMargin();

        mMargin.setText(margin);
        mRemainingDays.setText(remainingDays);

        //send notification when inhaler is running out
        String content = null;
        if(Integer.valueOf(margin) < MARGIN_MIN){
            content = "Less than 20 puffs left in your inhaler, remember to get a new one soon!";
        }

        if (Integer.valueOf(remainingDays) < REMAINDAYS_MIN){
            content = "Inhaler will expired in 5 days! remember to get a new one soon!";
        }

        if(content != null){
            sendNotification(content);
        }
    }

    public void getInhalerInfo(final InhalerInfoSetInterface inhalerInfoSetInterface){

        mDatabase.collection("inhaler").whereEqualTo("childUid", childUID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Inhaler inhaler = new Inhaler();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                inhaler.setFirstUsageDate(document.getString("firstUsageDate"));
                                inhaler.setMargin(document.getString("margin"));
                                inhaler.setChildUid(document.getString("childUid"));
                            }
                            inhalerInfoSetInterface.inhalerSetter(inhaler);
                        }
                    }
                });
    }

    public interface InhalerInfoSetInterface {
        void inhalerSetter(Inhaler inhaler);
    }

    private interface FireStoreCallback {
        void onCallback();
    }

    public void receiveNotification(final String collectionPath){

        DocumentReference mDocRef = mDatabase.collection(collectionPath).document(childUID);
        mDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, "onEvent: Listen Failed",e);
                    return;
                }

                if(documentSnapshot != null && documentSnapshot.exists()){
                    if(snapShotFlag){

                        String content = "";
                        switch(collectionPath){
                            case ASTHMA_REMINDER:
                                content = "Your child is feeling uncomfortable now!";
                                break;

                            case INHALER_REMINDER:
                                content = "Your child is taking 2 puffs from inhaler now!";
                                break;

                            case FINE_REMINDER:
                                content = "Your child is fine now!";
                                break;
                        }

                        snapShotFlag = false;
                        sendNotification(content);
                    }else{
                        snapShotFlag =true;
                    }
                }
            }
        });
    }

    private void sendNotification(String content) {

        String id = "01";
        String name = "01";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .setContentTitle("Intellihaling")
                    .setContentText(content)
                    .setTicker("Ticker")
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        }
        notificationManager.notify(0,notification);

    }

    public void main_enter_start_fr(View view){

        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        intent.putExtra("role", role);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    public void main_enter_chart_fr(View view){

        Intent intent = new Intent(MainActivity.this, AsthmaAttackDetailActivity.class);
        intent.putExtra("childUID", childUID);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    public void main_enter_help_fr(View view){

        Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
        intent.putExtra("role", role);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    public void main_enter_profile_btn(View view){

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("role", role);
        startActivityForResult(intent, REQUEST_LOGIN);
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
