package com.project.ece1778_project_intellihaling.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    private static final String TAG = "PhotosAdapter";


    private Context mContext;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private String uID;

    public PhotosAdapter() {


        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        uID = user.getUid();
    }

    // inflates the item_photos layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotosAdapter.ViewHolder holder, final int position) {

    }


    // total number of pic
    @Override
    public int getItemCount() {
        return 0;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
